//
//  BlueToothPrinter.h
//  BlueToothPrint
//
//  Created by euky on 2016/10/21.
//  Copyright © 2016年 euky. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UartLib.h"

@interface BlueToothPrinter : UartLib

+ (instancetype)sharedInstance;

@end
