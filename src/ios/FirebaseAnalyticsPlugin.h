#import <Cordova/CDV.h>
#import "AppDelegate.h"

@interface FirebaseAnalyticsPlugin : CDVPlugin
+ (FirebaseAnalyticsPlugin *)firebaseAnalyticsPlugin;
- (void)logEvent:(CDVInvokedUrlCommand *)command;
- (void)logError:(CDVInvokedUrlCommand *)command;
- (void)setCrashlyticsUserId:(CDVInvokedUrlCommand*)command;
- (void)setScreenName:(CDVInvokedUrlCommand *)command;
- (void)setUserId:(CDVInvokedUrlCommand *)command;
- (void)setUserProperty:(CDVInvokedUrlCommand *)command;
- (void)forceCrashlytics:(CDVInvokedUrlCommand *)command;
- (void)setAnalyticsCollectionEnabled:(CDVInvokedUrlCommand *)command;
- (void)startTrace:(CDVInvokedUrlCommand *)command;
- (void)incrementCounter:(CDVInvokedUrlCommand *)command;
- (void)stopTrace:(CDVInvokedUrlCommand *)command;
- (void)setPerformanceCollectionEnabled:(CDVInvokedUrlCommand *)command;
@property(nonatomic, readwrite) NSMutableDictionary *traces;

@end
