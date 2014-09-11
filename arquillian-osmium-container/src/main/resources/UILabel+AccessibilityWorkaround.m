@interface UILabel (AccessibilityWorkaround)
@property(nonatomic, copy) NSString * accessibilityValue;
@end

@implementation UILabel (AccessibilityWorkaround)

@dynamic accessibilityValue;

-(NSString *) accessibilityValue {
    // Here we force UIKit to return Label value, not the accessibility label
    return self.text;
}