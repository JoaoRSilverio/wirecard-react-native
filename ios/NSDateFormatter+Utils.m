//
//  NSDateFormatter+Utils.m
//  kussbuss
//
//  Created by Joao Silverio on 07/03/2019.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "NSDateFormatter+Utils.h"

@implementation NSDateFormatter (Utils)
  
+ (NSDateFormatter *)timestampDateFormatter {
  static dispatch_once_t onceToken;
  static NSDateFormatter *formatter;
  dispatch_once(&onceToken, ^{
    formatter = [NSDateFormatter new];
    NSLocale *enUSPOSIXLocale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
    formatter.locale = enUSPOSIXLocale;
    formatter.dateFormat = @"yyyy-MM-dd'T'HH:mm:ssZZZZZ";
    formatter.timeZone = [NSTimeZone timeZoneWithName:@"UTC"];
  });
  return [formatter copy];
}
  
  @end
