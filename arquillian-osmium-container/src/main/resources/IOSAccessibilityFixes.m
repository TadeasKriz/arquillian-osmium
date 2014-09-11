//#import <UIKit/UIKit.h>

@interface UILabel (AccessibilityFix)
@property(nonatomic, copy) NSString * accessibilityValue;
@end

@implementation UILabel (AccessibilityFix)

@dynamic accessibilityValue;

-(NSString *) accessibilityValue {
    // Here we force UIKit to return Label value, not the accessibility label
    return self.text;
}

@end

@interface UITableViewCell (AccessibilityFix)
@property(nonatomic, copy) NSString *accessibilityValue;
@end

@implementation UITableViewCell (AccessibilityFix)

@dynamic accessibilityValue;

-(NSString*)accessibilityValue {
    // Here we force UIKit to return child Label value, not the accessibility label
    return self.textLabel.text;
}

@end