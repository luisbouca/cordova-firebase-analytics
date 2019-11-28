#import "FirebaseAnalyticsPlugin.h"
#import <Cordova/CDV.h>
#import "AppDelegate.h"
@import Fabric;
@import Crashlytics;
@import FirebaseAnalytics;
@import FirebasePerformance;

#ifndef NSFoundationVersionNumber_iOS_9_x_Max
#define NSFoundationVersionNumber_iOS_9_x_Max 1299
#endif

@implementation FirebaseAnalyticsPlugin

@synthesize traces;

static FirebaseAnalyticsPlugin *firebaseAnalyticsPlugin;

+ (FirebaseAnalyticsPlugin *) firebaseAnalyticsPlugin {
    return firebaseAnalyticsPlugin;
}

- (void)pluginInitialize {
    NSLog(@"FirebaseAnalyticsPlugin - Starting Firebase Analytics plugin");
    firebaseAnalyticsPlugin = self;
}

//
// Analytics
//
- (void)setAnalyticsCollectionEnabled:(CDVInvokedUrlCommand *)command {
     [self.commandDelegate runInBackground:^{
        BOOL enabled = [[command argumentAtIndex:0] boolValue];

        [[FIRAnalyticsConfiguration sharedInstance] setAnalyticsCollectionEnabled:enabled];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
     }];
}

- (void)logEvent:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSString* name = [command.arguments objectAtIndex:0];
        NSDictionary *parameters;
        @try {
            NSString *description = NSLocalizedString([command argumentAtIndex:1 withDefault:@"No Message Provided"], nil);
            parameters = @{ NSLocalizedDescriptionKey: description };
        }
        @catch (NSException *execption) {
            parameters = [command argumentAtIndex:1];
        }

        [FIRAnalytics logEventWithName:name parameters:parameters];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)setScreenName:(CDVInvokedUrlCommand *)command {
    NSString* name = [command.arguments objectAtIndex:0];

    [FIRAnalytics setScreenName:name screenClass:NULL];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setUserId:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSString* id = [command.arguments objectAtIndex:0];

        [FIRAnalytics setUserID:id];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)setUserProperty:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSString* name = [command.arguments objectAtIndex:0];
        NSString* value = [command.arguments objectAtIndex:1];

        [FIRAnalytics setUserPropertyString:value forName:name];

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

//
// Crashlytics
//
- (void)logError:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSString* errorMessage = [command.arguments objectAtIndex:0];
        CLSNSLog(@"FirebaseAnalyticsPlugin - %@", errorMessage);
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
}

- (void)setCrashlyticsUserId:(CDVInvokedUrlCommand *)command {
    NSString* userId = [command.arguments objectAtIndex:0];

    [CrashlyticsKit setUserIdentifier:userId];
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)forceCrashlytics:(CDVInvokedUrlCommand *)command {
    [[Crashlytics sharedInstance] crash];
}

//
// Performace
//
- (void)startTrace:(CDVInvokedUrlCommand *)command {

    [self.commandDelegate runInBackground:^{
        NSString* traceName = [command.arguments objectAtIndex:0];
        FIRTrace *trace = [self.traces objectForKey:traceName];

        if (self.traces == nil) {
            self.traces = [NSMutableDictionary new];
        }

        if (trace == nil) {
            trace = [FIRPerformance startTraceWithName:traceName];
            [self.traces setObject:trace forKey:traceName ];

        }

        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

    }];
}

- (void)incrementCounter:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSString* traceName = [command.arguments objectAtIndex:0];
        NSString* counterNamed = [command.arguments objectAtIndex:1];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        FIRTrace *trace = (FIRTrace*)[self.traces objectForKey:traceName];

        if (trace != nil) {
            [trace incrementCounterNamed:counterNamed];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Trace not found"];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

    }];
}

- (void)stopTrace:(CDVInvokedUrlCommand *)command {
    [self.commandDelegate runInBackground:^{
        NSString* traceName = [command.arguments objectAtIndex:0];
        CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        FIRTrace *trace = [self.traces objectForKey:traceName];

        if (trace != nil) {
            [trace stop];
            [self.traces removeObjectForKey:traceName];
            [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        } else {
            pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Trace not found"];
        }

        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

    }];
}

- (void)setPerformanceCollectionEnabled:(CDVInvokedUrlCommand *)command {
     [self.commandDelegate runInBackground:^{
         BOOL enabled = [[command argumentAtIndex:0] boolValue];

         [[FIRPerformance sharedInstance] setDataCollectionEnabled:enabled];

         CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];

         [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
     }];
}

@end
