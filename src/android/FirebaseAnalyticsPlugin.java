package com.outsystems.firebaseanalyticsplugin;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * This class echoes a string called from JavaScript.
 */
public class FirebaseAnalyticsPlugin extends CordovaPlugin {

	private FirebaseAnalytics mFirebaseAnalytics;
  	private static final String TAG = "FirebasePlugin";
	protected static final String KEY = "badge";

	private static boolean inBackground = true;
	private static CallbackContext tokenRefreshCallbackContext;

	@Override
	protected void pluginInitialize() {
		final Context context = this.cordova.getActivity().getApplicationContext();
		final Bundle extras = this.cordova.getActivity().getIntent().getExtras();
		this.cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				Log.d(TAG, "Starting Firebase plugin");
				mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
				mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
			}
		});
	}

	@Override
  	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals("logEvent")) {
			this.logEvent(callbackContext, args.getString(0), args.getJSONObject(1));
			return true;
		} else if (action.equals("logError")) {
			this.logError(callbackContext, args.getString(0));
			return true;
		} else if (action.equals("setCrashlyticsUserId")) {
			this.setCrashlyticsUserId(callbackContext, args.getString(0));
			return true;
		} else if (action.equals("setScreenName")) {
			this.setScreenName(callbackContext, args.getString(0));
			return true;
		} else if (action.equals("setUserId")) {
			this.setUserId(callbackContext, args.getString(0));
			return true;
		} else if (action.equals("setUserProperty")) {
			this.setUserProperty(callbackContext, args.getString(0), args.getString(1));
			return true;
		} else if (action.equals("forceCrashlytics")) {
			this.forceCrashlytics(callbackContext);
			return true;
		} else if (action.equals("setAnalyticsCollectionEnabled")) {
			this.setAnalyticsCollectionEnabled(callbackContext, args.getBoolean(0));
			return true;
		} else if (action.equals("startTrace")) {
			this.startTrace(callbackContext, args.getString(0));
			return true;
		} else if (action.equals("incrementCounter")) {
			this.incrementCounter(callbackContext, args.getString(0), args.getString(1));
			return true;
		} else if (action.equals("stopTrace")) {
			this.stopTrace(callbackContext, args.getString(0));
			return true;
		} else if (action.equals("addTraceAttribute")) {
			this.addTraceAttribute(callbackContext, args.getString(0), args.getString(1), args.getString(2));
			return true;  
		}else if (action.equals("setPerformanceCollectionEnabled")) {
			this.setPerformanceCollectionEnabled(callbackContext, args.getBoolean(0));
			return true;
		}
		return false;
  	}

	@Override
	public void onPause(boolean multitasking) {
		FirebaseAnalyticsPlugin.inBackground = true;
	}

	@Override
	public void onResume(boolean multitasking) {
		FirebaseAnalyticsPlugin.inBackground = false;
	}

	public static boolean inBackground() {
		return FirebaseAnalyticsPlugin.inBackground;
	}

	// 
	// Analytics
	//
	private void logEvent(final CallbackContext callbackContext, final String name, final JSONObject params) throws JSONException {
		Log.d(TAG, "logEvent called. name: " + name);
		final Bundle bundle = new Bundle();
		Iterator iter = params.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			Object value = params.get(key);

			if (value instanceof Integer || value instanceof Double) {
				bundle.putFloat(key, ((Number) value).floatValue());
			} else {
				bundle.putString(key, value.toString());
			}
		}

		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					mFirebaseAnalytics.logEvent(name, bundle);
					callbackContext.success();
					Log.d(TAG, "logEvent success");
				} catch (Exception e) {
					Crashlytics.logException(e);
					callbackContext.error(e.getMessage());
				}
			}
		});
	}

	private void setScreenName(final CallbackContext callbackContext, final String name) {
		Log.d(TAG, "setScreenName called. name: " + name);
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				try {
					mFirebaseAnalytics.setCurrentScreen(cordova.getActivity(), name, null);
					callbackContext.success();
				Log.d(TAG, "setScreenName success");
				} catch (Exception e) {
					Crashlytics.logException(e);
					callbackContext.error(e.getMessage());
				}
			}
		});
	}

	private void setUserId(final CallbackContext callbackContext, final String id) {
		Log.d(TAG, "setUserId called. id: " + id);
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					mFirebaseAnalytics.setUserId(id);
					callbackContext.success();
					Log.d(TAG, "setUserId success");
				} catch (Exception e) {
					Crashlytics.logException(e);
					callbackContext.error(e.getMessage());
				}
			}
		});
	}

	private void setUserProperty(final CallbackContext callbackContext, final String name, final String value) {
		Log.d(TAG, "setUserProperty called. name: " + name + " value: " + value);
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					mFirebaseAnalytics.setUserProperty(name, value);
					callbackContext.success();
					Log.d(TAG, "setUserProperty success");
				} catch (Exception e) {
					Crashlytics.logException(e);
					callbackContext.error(e.getMessage());
				}
			}
		});
	}

	private void setAnalyticsCollectionEnabled(final CallbackContext callbackContext, final boolean enabled) {
		Log.d(TAG, "setAnalyticsCollectionEnabled called. enabled: " + (enabled ? "true" : "false"));
		final FirebaseAnalyticsPlugin self = this;
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					mFirebaseAnalytics.setAnalyticsCollectionEnabled(enabled);
					callbackContext.success();
					Log.d(TAG, "setAnalyticsCollectionEnabled success");
				} catch (Exception e) {
					Crashlytics.log(e.getMessage());
					callbackContext.error(e.getMessage());
				}
			}
		});
	}

	// 
	// Crashlytics
	//
	private void forceCrashlytics(final CallbackContext callbackContext) {
		Log.d(TAG, "forceCrashlytics called");
		final FirebaseAnalyticsPlugin self = this;
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				Crashlytics.getInstance().crash();
			}
		});
	}
	
	private void logError(final CallbackContext callbackContext, final String message) throws JSONException {
		Log.d(TAG, "logError called. message: " + message);
		cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				try {
					Crashlytics.logException(new Exception(message));
					callbackContext.success(1);
					Log.d(TAG, "logError success");
				} catch (Exception e) {
					Crashlytics.log(e.getMessage());
					callbackContext.error(e.getMessage());
				}
			}
		});
	}

	private void setCrashlyticsUserId(final CallbackContext callbackContext, final String userId) {
		Log.d(TAG, "setCrashlyticsUserId called. userId: " + userId);
		cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				try {
					Crashlytics.setUserIdentifier(userId);
					callbackContext.success();
					Log.d(TAG, "setCrashlyticsUserId success");
				} catch (Exception e) {
					Crashlytics.logException(e);
					callbackContext.error(e.getMessage());
				}
			}
		});
	}


  // 
  // Performance monitoring
  //
  private HashMap<String, Trace> traces = new HashMap<String,Trace>();

  private void startTrace(final CallbackContext callbackContext, final String name) {
    Log.d(TAG, "startTrace called. name: " + name);
    final FirebaseAnalyticsPlugin self = this;
    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        try {
          Trace myTrace = null;
          if (self.traces.containsKey(name)) {
            myTrace = self.traces.get(name);
          }
          if (myTrace == null) {
            myTrace = FirebasePerformance.getInstance().newTrace(name);
            myTrace.start();
            self.traces.put(name, myTrace);
          }
          callbackContext.success();
          Log.d(TAG, "startTrace success");
        } catch (Exception e) {
          Crashlytics.logException(e);
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  private void incrementCounter(final CallbackContext callbackContext, final String name, final String counterNamed) {
    Log.d(TAG, "incrementCounter called. name: " + name + " counterNamed: " + counterNamed);
    final FirebaseAnalyticsPlugin self = this;
    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        try {
          Trace myTrace = null;
          if (self.traces.containsKey(name)) {
            myTrace = self.traces.get(name);
          }
          if (myTrace != null && myTrace instanceof Trace) {
            myTrace.incrementMetric(counterNamed,1);
            callbackContext.success();
            Log.d(TAG, "incrementCounter success");
          } else {
            callbackContext.error("Trace not found");
            Log.d(TAG, "incrementCounter trace not found");
          }
        } catch (Exception e) {
          Crashlytics.logException(e);
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  private void stopTrace(final CallbackContext callbackContext, final String name) {
    Log.d(TAG, "stopTrace called. name: " + name);
    final FirebaseAnalyticsPlugin self = this;
    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        try {
          Trace myTrace = null;
          if (self.traces.containsKey(name)) {
            myTrace = self.traces.get(name);
          }
          if (myTrace != null && myTrace instanceof Trace) {
            myTrace.stop();
            self.traces.remove(name);
            callbackContext.success();
            Log.d(TAG, "stopTrace success");
          } else {
            callbackContext.error("Trace not found");
            Log.d(TAG, "stopTrace trace not found");
          }
        } catch (Exception e) {
          Crashlytics.logException(e);
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  private void addTraceAttribute(final CallbackContext callbackContext, final String traceName, final String attribute, final String value) {
    Log.d(TAG, "addTraceAttribute called. traceName: " + traceName + " attribute: " + attribute + " value: " + value);
    final FirebaseAnalyticsPlugin self = this;
    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        try {
          Trace myTrace = null;
          if (self.traces.containsKey(traceName)) {
            myTrace = self.traces.get(traceName);
          }
          if (myTrace != null && myTrace instanceof Trace) {
            myTrace.putAttribute(attribute, value);
            callbackContext.success();
            Log.d(TAG, "addTraceAttribute success");
          } else {
            callbackContext.error("Trace not found");
            Log.d(TAG, "addTraceAttribute trace not found");
          }
        } catch (Exception e) {
          Crashlytics.log(e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }

  private void setPerformanceCollectionEnabled(final CallbackContext callbackContext, final boolean enabled) {
    Log.d(TAG, "setPerformanceCollectionEnabled called. enabled: " + (enabled ? "true" : "false"));
    final FirebaseAnalyticsPlugin self = this;
    cordova.getThreadPool().execute(new Runnable() {
      public void run() {
        try {
          FirebasePerformance.getInstance().setPerformanceCollectionEnabled(enabled);
          callbackContext.success();
          Log.d(TAG, "setPerformanceCollectionEnabled success");
        } catch (Exception e) {
          Crashlytics.log(e.getMessage());
          callbackContext.error(e.getMessage());
        }
      }
    });
  }
}
