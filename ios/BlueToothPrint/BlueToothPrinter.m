//
//  BlueToothPrinter.m
//  BlueToothPrint
//
//  Created by euky on 2016/10/21.
//  Copyright © 2016年 euky. All rights reserved.
//

#import "BlueToothPrinter.h"

@implementation BlueToothPrinter

+ (instancetype)sharedInstance {
    static BlueToothPrinter *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[BlueToothPrinter alloc] init];
    });
    return sharedInstance;
}
@end
