/*
 * TasksUtils.java
 * 
 * TKWeek (c) Thomas Künneth 2013 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.tkweek.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.thomaskuenneth.tkweek.types.Task;
import com.thomaskuenneth.tkweek.types.TaskList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TasksUtils {

    private static final String TAG = TasksUtils.class.getSimpleName();

    private static final String SELFLINK = "selfLink";
    private static final String ID = "id";
    private static final String ITEMS = "items";
    private static final String DUE = "due";
    private static final String NOTES = "notes";
    private static final String TITLE = "title";
    private static final String STATUS = "status";
    private static final String COMPLETED = "completed";
    private static final String NEEDS_ACTION = "needsAction";

    private static final Pattern PATTERN_CHARSET
            = Pattern.compile(".*charset\\s*=\\s*(.*)$");

    private static final DateFormat FORMAT_RFC_3339 =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);

    // Konto-Typ
    private static final String TYPE = "com.google";

    // wird bei der Ermittlung des Auth Tokens benötigt
    // Hinweis: in der Doku steht "Manage your tasks", aber das funktioniert nicht mehr
    private static final String AUTH_TOKEN_TYPE = "cl";

    private static final String GET_TASKS_ASYNC = "_getTasksAsync";
    private static final String GET_TASK_LISTS = "_getTaskLists";
    private static final String ADD_TASK = "_addTask";
    private static final String MARK_FINISHED = "_markFinished";

    // Zugriff auf https://code.google.com/apis/console beantragen
    private static final String API_KEY = "...";

    private final Message message;
    private final Activity _a;
    private final List<Task> _list;
    private final AccountManager accountManager;

    private String token;

    private boolean _showCompleted;
    private String _id;
    private String _title;
    private String _notes;
    private String _selfLink;
    private Object _data;
    private Date _dueDate;
    private boolean _finished;

    private TasksUtils(Handler handler, Activity activity) {
        message = new Message();
        message.setTarget(handler);
        _a = activity;
        _list = new ArrayList<>();
        accountManager = AccountManager.get(_a);
    }

    public static TasksUtils getInstance(Handler handler, Activity activity) {
        TasksUtils result = null;
        if (TKWeekUtils.canGetAccounts(activity)) {
            result = new TasksUtils(handler, activity);
        }
        return result;
    }

    public static boolean hasGoogleAccount(Activity a) {
        AccountManager accountManager = AccountManager.get(a);
        Account[] accounts = accountManager.getAccountsByType(TYPE);
        return accounts.length == 1;
    }

    public static boolean isStatusCode200(Message msg) {
        Integer response = (Integer) msg.obj;
        return !((response == null) || (response != 200));
    }

    public boolean getTasksAsync(boolean showCompleted) {
        _showCompleted = showCompleted;
        return doIt(GET_TASKS_ASYNC);
    }

    public boolean getTaskListsAsync() {
        return doIt(GET_TASK_LISTS);
    }

    public boolean addTask(String id, String title, String notes, Date dueDate) {
        _id = id;
        _title = title;
        _notes = notes;
        _dueDate = dueDate;
        return doIt(ADD_TASK);
    }

    public boolean markFinished(String selfLink, Object data, boolean finished) {
        _selfLink = selfLink;
        _data = data;
        _finished = finished;
        return doIt(MARK_FINISHED);
    }

    /**
     * Ruft die übergebene Methode auf, wenn ein auth token ermittelt werden konnte.
     *
     * @param methodName Methode
     * @return true, wenn ein auth token ermittelt werden konnte
     */
    private boolean doIt(final String methodName) {
        boolean success = false;
        try {
            accountManager.invalidateAuthToken(TYPE, token);
            Account[] accounts = accountManager.getAccountsByType(TYPE);
            Log.d(TAG, String.format(Locale.US, "%d accounts", accounts.length));
            if (accounts.length == 1) {
                AccountManagerCallback<Bundle> cb = new AccountManagerCallback<Bundle>() {

                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Log.d(TAG, "called run() from getAuthToken()");
                        try {
                            Bundle result = future.getResult();
                            token = result.getString(AccountManager.KEY_AUTHTOKEN);
                            Runnable r = new Runnable() {

                                @Override
                                public void run() {
                                    Log.d(TAG, String.format("calling %s", methodName));
                                    try {
                                        Method method = TasksUtils.this.getClass().getDeclaredMethod(
                                                methodName, (Class[]) null);
                                        method.setAccessible(true);
                                        method.invoke(TasksUtils.this, (Object[]) null);
                                    } catch (NoSuchMethodException | IllegalArgumentException
                                            | IllegalAccessException | InvocationTargetException e) {
                                        Log.e(TAG, "run()", e);
                                    }
                                    message.getTarget().dispatchMessage(message);
                                    accountManager.invalidateAuthToken(TYPE, token);
                                }

                            };
                            new Thread(r).start();
                        } catch (OperationCanceledException | AuthenticatorException | IOException e) {
                            Log.e(TAG, "run()", e);
                        }
                    }
                };
                accountManager.getAuthToken(accounts[0], AUTH_TOKEN_TYPE,
                        null, _a, cb, null);
                success = true;
            }
        } catch (Throwable tr) {
            Log.e(TAG, "doIt()", tr);
        }
        return success;
    }

    private int getFromServer(StringBuilder sb, String url, String token, JSONObject json, String method) {
        HttpURLConnection httpURLConnection = null;
        int responseCode = 0;
        try {
            URL _url = new URL(url);
            httpURLConnection = (HttpURLConnection) _url.openConnection();
            // Verbindung konfigurieren
            // httpURLConnection.setRequestProperty("Authorization", "OAuth " + token);
            httpURLConnection.setRequestProperty("Authorization", "GoogleLogin auth=" + token);

            // Daten senden?
            if (json != null) {
                byte[] data = json.toString().getBytes();
                httpURLConnection.setDoOutput(true);
                if (method != null) {
                    httpURLConnection.setRequestMethod(method);
                }
                httpURLConnection.setRequestProperty("Content-Type",
                        "application/json; charset="
                                + Charset.defaultCharset().name());
                httpURLConnection.setFixedLengthStreamingMode(data.length);
                httpURLConnection.getOutputStream().write(data);
                httpURLConnection.getOutputStream().flush();
            }
            // character set ermitteln oder raten
            String contentType = httpURLConnection.getContentType();
            String charSet = "ISO-8859-1";
            if (contentType != null) {
                Matcher m = PATTERN_CHARSET.matcher(contentType);
                if (m.matches()) {
                    charSet = m.group(1);
                }
            }
            // ggf. response holen
            responseCode = httpURLConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStreamReader inputStreamReader = new InputStreamReader(
                        httpURLConnection.getInputStream(), charSet);
                BufferedReader bufferedReader = new BufferedReader(
                        inputStreamReader);
                int i;
                while ((i = bufferedReader.read()) != -1) {
                    sb.append((char) i);
                }
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    // ein Fehler beim Schließen wird bewusst ignoriert
                }
            } else {
                Log.d(TAG, "responseCode: " + responseCode);
            }
        } catch (Throwable tr) { // MalformedURLException, IOException,
            // NullPointerException,
            // UnsupportedEncodingException
            Log.e(TAG, "Fehler beim Zugriff auf " + url, tr);
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return responseCode;
    }

    private List<TaskList> getTaskLists() {
        StringBuilder sb = new StringBuilder();
        List<TaskList> result = new ArrayList<>();
        int responseCode = getFromServer(sb,
                "https://www.googleapis.com/tasks/v1/users/@me/lists?key="
                        + API_KEY, token, null, null);
        if (responseCode == 200) {
            try {
                JSONObject jsonLists = new JSONObject(sb.toString());
                if (jsonLists.has(ITEMS)) {
                    JSONArray jsonItems = jsonLists.getJSONArray(ITEMS);
                    for (int i = 0; i < jsonItems.length(); i++) {
                        JSONObject jsonItem = jsonItems.getJSONObject(i);
                        String title = null;
                        if (jsonItem.has(TITLE)) {
                            title = jsonItem.getString(TITLE);
                        }
                        String id = null;
                        if (jsonItem.has(ID)) {
                            id = jsonItem.getString(ID);
                        }
                        TaskList list = new TaskList(title, id);
                        result.add(list);
                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "getTaskLists()", e);
            }
        }
        return result;
    }

    private void getTasks(List<Task> list, String id, String listTitle,
                          boolean showCompleted) {
        String url = "https://www.googleapis.com/tasks/v1/lists/" + id
                + "/tasks?showCompleted=" + showCompleted
                + "&key=" + API_KEY;
        StringBuilder sb = new StringBuilder();
        int responseCode = getFromServer(sb, url, token, null, null);
        if (responseCode == 200) {
            try {
                JSONObject jsonTasks = new JSONObject(sb.toString());
                if (jsonTasks.has(ITEMS)) {
                    JSONArray tasksItems = jsonTasks.getJSONArray(ITEMS);
                    for (int j = 0; j < tasksItems.length(); j++) {
                        JSONObject task = tasksItems.getJSONObject(j);
                        String taskTitle = null;
                        if (task.has(TITLE)) {
                            taskTitle = task.getString(TITLE);
                        }
                        String taskNotes = null;
                        if (task.has(NOTES)) {
                            taskNotes = task.getString(NOTES);
                        }
                        Date taskDue = null;
                        if (task.has(DUE)) {
                            String due = task.getString(DUE);
                            Calendar cal = DateUtilities.getCalendarFromRFC3339String(due);
                            taskDue = cal.getTime();
                        }
                        String selfLink = null;
                        if (task.has(SELFLINK)) {
                            selfLink = task.getString(SELFLINK);
                        }
                        boolean completed = false;
                        if (task.has(STATUS)) {
                            completed = COMPLETED.equalsIgnoreCase(task.getString(STATUS));
                        }
                        Task t = new Task(taskTitle, taskNotes, taskDue, listTitle, selfLink, completed);
                        list.add(t);
                    }
                } else {
                    Log.d(TAG, "   -> keine Elemente");
                }
            } catch (JSONException e) {
                Log.e(TAG, "getTasks()", e);
            }
        }
    }

    private void _getTasksAsync() {
        List<TaskList> taskLists = getTaskLists();
        for (TaskList list : taskLists) {
            getTasks(_list, list.id, list.title, _showCompleted);
        }
        // sortieren
        Collections.sort(_list, new Comparator<Task>() {

            private final Date current = new Date();

            @Override
            public int compare(Task lhs, Task rhs) {
                if ((lhs == null) && (rhs == null)) {
                    return 0;
                }
                if (lhs == null) {
                    return -1;
                } else if (rhs == null) {
                    return 1;
                }
                Date dueLHS = lhs.due;
                if (dueLHS == null) {
                    dueLHS = current;
                }
                Date dueRHS = rhs.due;
                if (dueRHS == null) {
                    dueRHS = current;
                }
                if (dueLHS.before(dueRHS)) {
                    return -1;
                } else if (dueRHS.before(dueLHS)) {
                    return 1;
                }
                return lhs.title.compareToIgnoreCase(rhs.title);
            }
        });
        message.obj = _list;
    }

    private void _getTaskLists() {
        message.obj = getTaskLists();
    }

    private void _addTask() {
        String url = "https://www.googleapis.com/tasks/v1/lists/" + _id
                + "/tasks?key=" + API_KEY + "&";
        JSONObject json = new JSONObject();
        Integer response = null;
        try {
            json.put(TITLE, _title);
            json.put(NOTES, _notes);
            if (_dueDate != null) {
                json.put(DUE, FORMAT_RFC_3339.format(_dueDate));
            }
            StringBuilder sb = new StringBuilder();
            response = getFromServer(sb, url, token, json, "POST");
        } catch (JSONException e) {
            Log.e(TAG, "_addTask()", e);
        }
        message.obj = response;
    }

    /**
     * Setzt eine Aufgabe auf completed oder needsAction.
     * Im Erfolgsfall erhält obj das übergebene data-Objekt, sonst null.
     */
    private void _markFinished() {
        StringBuilder sb = new StringBuilder();
        String url = _selfLink + "?key=" + API_KEY;
        int responseCode = getFromServer(sb, url, token, null, null);
        if (responseCode == 200) {
            try {
                JSONObject jsonTask = new JSONObject(sb.toString());
                jsonTask.put(STATUS, _finished ? COMPLETED : NEEDS_ACTION);
                if (!_finished) {
                    jsonTask.put(COMPLETED, null);
                }
                sb.setLength(0);
                responseCode = getFromServer(sb, url, token, jsonTask, "PUT");
            } catch (JSONException e) {
                Log.e(TAG, "_markFinished()", e);
            }
        }
        if (responseCode == 200) {
            message.obj = _data;
        } else {
            message.obj = null;
        }
    }
}
