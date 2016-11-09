//
//  BluetoothPrinter.h
//  BluetoothPrint
//
//  Created by NovaCloud on 16/11/4.
//  Copyright © 2016年 NovaCloud. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UartLib.h"

@interface BluetoothPrinter : UartLib
+ (instancetype)sharedInstance;
@property (nonatomic) NSInteger delay;
@end
