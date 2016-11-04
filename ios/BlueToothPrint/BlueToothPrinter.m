//
//  BluetoothPrinter.m
//  BluetoothPrint
//
//  Created by NovaCloud on 16/11/4.
//  Copyright © 2016年 NovaCloud. All rights reserved.
//

#import "BluetoothPrinter.h"

@implementation BluetoothPrinter
+ (instancetype)sharedInstance {
    static BluetoothPrinter *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[BluetoothPrinter alloc] init];
    });
    return sharedInstance;
}
@end
