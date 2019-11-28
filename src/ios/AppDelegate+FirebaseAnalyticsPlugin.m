#import "AppDelegate+FirebaseAnalyticsPlugin.h"
#import "FirebaseAnalyticsPlugin.h"
@import Fabric;
@import Crashlytics;
@import FirebaseAnalytics;
#import <objc/runtime.h>

#define kApplicationInBackgroundKey @"applicationInBackground"
#define kDelegateKey @"delegate"

@implementation AppDelegate (FirebaseAnalyticsPlugin)

#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0

- (void)setDelegate:(id)delegate {
    objc_setAssociatedObject(self, kDelegateKey, delegate, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (id)delegate {
    return objc_getAssociatedObject(self, kDelegateKey);
}

#endif

+ (void)load {
    Method original = class_getInstanceMethod(self, @selector(application:didFinishLaunchingWithOptions:));
    Method swizzled = class_getInstanceMethod(self, @selector(application:swizzledDidFinishLaunchingWithOptions:));
    method_exchangeImplementations(original, swizzled);
}

- (void)setApplicationInBackground:(NSNumber *)applicationInBackground {
    objc_setAssociatedObject(self, kApplicationInBackgroundKey, applicationInBackground, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (NSNumber *)applicationInBackground {
    return objc_getAssociatedObject(self, kApplicationInBackgroundKey);
}

- (BOOL)application:(UIApplication *)application swizzledDidFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    NSLog(@"FirebaseAnalyticsPlugin - Finished launching");
    [self application:application swizzledDidFinishLaunchingWithOptions:launchOptions];

    [FIRApp configure];
    
    self.applicationInBackground = @(YES);
    
    return YES;
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    self.applicationInBackground = @(NO);
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    self.applicationInBackground = @(YES);
}

@end