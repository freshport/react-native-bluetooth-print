//
//  BlueToothPrint.m
//  BlueToothPrint
//
//  Created by euky on 2016/10/21.
//  Copyright © 2016年 euky. All rights reserved.
//

#import "BluetoothPrint.h"
#import "BluetoothPrinter.h"
#import "RCTBridgeModule.h"
#import <CoreText/CoreText.h>

#define LINE_MAX_CHARACTERISTICS 48
#define LINE_MAX_WITH_SMALL_CHARACTERISTICS 72
#define PRINT_DELAY_OFFSET 3

@interface BluetoothPrint()<RCTBridgeModule>

@property (nonatomic, strong) BluetoothPrinter *printer;
@property (nonatomic, strong) CBPeripheral *connectedPer;

@end

@implementation BluetoothPrint

- (BluetoothPrinter *)printer {
    if (!_printer) {
        _printer = [BluetoothPrinter sharedInstance];
    }
    return  _printer;
}

- (CBPeripheral *)connectedPer {
    if (!_connectedPer) {
        _connectedPer = self.printer.retrieveConnectedPeripherals.firstObject;
    }
    return _connectedPer;
}

RCT_EXPORT_MODULE();
RCT_EXPORT_METHOD(setDelay:(NSUInteger *)delay) {
    self.printer.delay = delay;
}
RCT_EXPORT_METHOD(hasConnectedToAPrinter:(RCTResponseSenderBlock)callback) {
    callback(@[[NSNull null], self.connectedPer ? @YES : @NO]);
}
RCT_EXPORT_METHOD(orderPrint:(NSArray *)rawData) {
    if (!self.connectedPer) {
        return;
    }
    
    for (NSDictionary *dic in rawData) {
        
        [self print: [NSString stringWithFormat:@"%@", dic[@"user_company"]] align:kCTTextAlignmentCenter];
        [self printSmallJustified:@[
                                    [NSString stringWithFormat:@"No.%@", dic[@"no"]],
                                    dic[@"date"]
                                    ]];
        [self print:[NSString stringWithFormat:@"客户公司名称:%@", dic[@"company"]] align:kCTTextAlignmentLeft];
        [self print:[NSString stringWithFormat:@"客户公司联系人:%@", dic[@"saler"][@"user"]] align:kCTTextAlignmentLeft];
        [self printJustified:@[@"品名/品种/规格/件重",
                               @"退损 数量 单价   金额"
                               ]];
        
        for (NSDictionary *dicInfo in dic[@"list"]) {
            NSString *returnnumStr = [[NSString stringWithFormat:@"%@", dicInfo[@"returnnum"]] stringByPaddingToLength:4 withString:@" " startingAtIndex:0];
            NSString *numStr = [[NSString stringWithFormat:@"%@", dicInfo[@"num"]] stringByPaddingToLength:4 withString:@" " startingAtIndex:0];
            NSString *priceStr = [[NSString stringWithFormat:@"%@", dicInfo[@"price"]] stringByPaddingToLength:4 withString:@" " startingAtIndex:0];
            NSMutableString *cashStr = [[NSMutableString alloc] initWithCapacity:6];
            for (NSInteger i = 0; i < 6 - (NSInteger)[[NSString stringWithFormat:@"%@", dicInfo[@"cash"]] length]; i++) {
                [cashStr appendString:@" "];
            }
            [cashStr appendString:[NSString stringWithFormat:@"%@", dicInfo[@"cash"]]];
            [self printJustified:@[ [self generateInfoVal:dicInfo],
                                    [NSString stringWithFormat:@"%@ %@ %@ %@",
                                     returnnumStr,
                                     numStr,
                                     priceStr,
                                     cashStr]
                                    ]];
            
        }
        
        NSMutableString *sumStr = [[NSMutableString alloc] initWithCapacity:12];
        for (NSInteger i = 0 ; i < 12 - (NSInteger)[[NSString stringWithFormat:@"%@", dic[@"sum"][@"sum"]] length]; i ++) {
            [sumStr appendString:@" "];
        }
        [sumStr appendString:[NSString stringWithFormat:@"%@", dic[@"sum"][@"sum"]]];
        [self print:[NSString stringWithFormat:@"合计%@", sumStr] align:kCTTextAlignmentRight];
        [self printJustified:@[
                               [NSString stringWithFormat:@"销售员:%@ %@", dic[@"user_saler"], dic[@"user_tel"]],
                               dic[@"type"]
                               ]];
        [self print:@"************************************************" align:kCTTextAlignmentCenter];
        [self print:@"注：本销售单等同于辉展市场巜销售成交单》" align:kCTTextAlignmentLeft];
        [self print:@"客户签名:\n\n\n\n\n\n" align:kCTTextAlignmentLeft];
        
        NSInteger delay = self.printer.delay == 0 ? 5 + PRINT_DELAY_OFFSET : self.printer.delay + PRINT_DELAY_OFFSET;
        if (delay < 0) {
            delay = 0
        }
        [NSThread sleepForTimeInterval:delay];
    }
}

- (void)printJustified:(NSArray *)printContent {
    NSData *data = nil;
    
    Byte caPrintFmt[8];
    /*初始化命令：ESC @ 即0x1b,0x40*/
    caPrintFmt[0] = 0x1b;
    caPrintFmt[1] = 0x40;
    //
    /*字符设置命令：ESC ! n即0x1b,0x21,n*/
    caPrintFmt[2] = 0x1b;
    caPrintFmt[3] = 0x33;
    
    caPrintFmt[4] = 0x16;
    
    NSData *cmdData =[[NSData alloc] initWithBytes:caPrintFmt length:5];
    [self.printer sendValue:self.connectedPer sendData:cmdData type:CBCharacteristicWriteWithResponse];
    
    NSStringEncoding enc = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    
    int count = 0;
    NSString *tmpStr = [printContent.firstObject stringByAppendingString:printContent[1]];
    for (int i = 0; i < [tmpStr length]; i++) {
        unichar ch = [tmpStr characterAtIndex:i];
        if (ch <= 127) {
            count++;
        } else {
            count = count + 2;
        }
    }
    int intend = LINE_MAX_CHARACTERISTICS - count;
    NSMutableString *intendStr = [[NSMutableString alloc] init];
    for (int i = 0; i < intend; i++) {
        [intendStr appendString:@" "];
    }
    
    NSMutableString *printString = [[NSMutableString alloc] init];
    [printString appendString:printContent.firstObject];
    [printString appendString:intendStr];
    [printString appendString:printContent[1]];
    
    data = [printString dataUsingEncoding:enc];
    [self.printer sendValue:self.connectedPer sendData:data type:CBCharacteristicWriteWithResponse];
    
    Byte printCmd[1];
    printCmd[0] = 0x0a;
    NSData * printCmdData = [[NSData alloc] initWithBytes:printCmd length:1];
    [self.printer sendValue:self.connectedPer sendData:printCmdData type:CBCharacteristicWriteWithResponse];
    
}


- (void)printSmallJustified:(NSArray *)printContent{
    NSData *data = nil;
    
    Byte caPrintFmt[8];
    /*初始化命令：ESC @ 即0x1b,0x40*/
    caPrintFmt[0] = 0x1b;
    caPrintFmt[1] = 0x40;
    //
    /*字符设置命令：ESC ! n即0x1b,0x21,n*/
    caPrintFmt[2] = 0x1b;
    caPrintFmt[3] = 0x21;
    
    caPrintFmt[4] = 0x0f;
    
    /*字符行间距 ESC 3 n 即0x1b 33 n  */
    caPrintFmt[5] = 0x1b;
    caPrintFmt[6] = 0x33;
    
    caPrintFmt[7] = 0x1e;
    
    NSData *cmdData =[[NSData alloc] initWithBytes:caPrintFmt length:8];
    [self.printer sendValue:self.connectedPer sendData:cmdData type:CBCharacteristicWriteWithResponse];
    
    NSStringEncoding enc = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    
    int count = 0;
    NSString *tmpStr = [printContent.firstObject stringByAppendingString:printContent[1]];
    for (int i = 0; i < [tmpStr length]; i++) {
        unichar ch = [tmpStr characterAtIndex:i];
        if (ch <= 127) {
            count++;
        } else {
            count = count + 2;
        }
    }
    int intend = LINE_MAX_WITH_SMALL_CHARACTERISTICS - count;
    NSMutableString *intendStr = [[NSMutableString alloc] init];
    for (int i = 0; i < intend; i++) {
        [intendStr appendString:@" "];
    }
    
    NSMutableString *printString = [[NSMutableString alloc] init];
    [printString appendString:printContent.firstObject];
    [printString appendString:intendStr];
    [printString appendString:printContent[1]];
    
    data = [printString dataUsingEncoding:enc];
    [self.printer sendValue:self.connectedPer sendData:data type:CBCharacteristicWriteWithResponse];
    
    Byte printCmd[1];
    printCmd[0] = 0x0a;
    NSData * printCmdData = [[NSData alloc] initWithBytes:printCmd length:1];
    [self.printer sendValue:self.connectedPer sendData:printCmdData type:CBCharacteristicWriteWithResponse];
    
}


- (void)print:(NSString *)printContent align:(CTTextAlignment)textAlignment{
    NSData  *data	= nil;
    //
    Byte caPrintFmt[8];
    /*初始化命令：ESC @ 即0x1b,0x40*/
    caPrintFmt[0] = 0x1b;
    caPrintFmt[1] = 0x40;
    //
    /*字符设置命令：ESC ! n即0x1b,0x21,n*/
    caPrintFmt[2] = 0x1b;
    caPrintFmt[3] = 0x33;
    
    caPrintFmt[4] = 0x16;
    //
    /*字符行间距设置命令 ESC a n即0x1b, 0x61 n*/
    caPrintFmt[5] = 0x1b;
    caPrintFmt[6] = 0x61;
    
    switch (textAlignment) {
        case kCTTextAlignmentCenter:
            caPrintFmt[7] = 0x01;
            break;
        case kCTTextAlignmentRight:
            caPrintFmt[7] = 0x02;
            break;
        default:
            caPrintFmt[7] = 0x00;
            break;
    }
    
    NSData *cmdData =[[NSData alloc] initWithBytes:caPrintFmt length:8];
    [self.printer sendValue:self.connectedPer sendData:cmdData type:CBCharacteristicWriteWithResponse];
    
    
    NSStringEncoding enc = CFStringConvertEncodingToNSStringEncoding(kCFStringEncodingGB_18030_2000);
    data = [printContent dataUsingEncoding:enc];
    [self.printer sendValue:self.connectedPer sendData:data type:CBCharacteristicWriteWithResponse];
    
    Byte printCmd[1];
    printCmd[0] = 0x0a;
    NSData * printCmdData = [[NSData alloc] initWithBytes:printCmd length:1];
    [self.printer sendValue:self.connectedPer sendData:printCmdData type:CBCharacteristicWriteWithResponse];
    
}



- (NSString *)generateInfoText:(NSDictionary *)dic {
    NSMutableString *ret = [[NSMutableString alloc] init];
    if ([self isValid:dic[@"product"]]) {
        [ret appendString:@"品名/"];
    }
    if ([self isValid:dic[@"note"]]) {
        [ret appendFormat:@"(%@)", dic[@"note"]];
    }
    if (![ret isEqualToString:@""]) {
        [ret appendString:@"/"];
    }
    if ([self isValid:dic[@"variety"]]) {
        [ret appendString:@"品种/"];
    }
    if ([self isValid:dic[@"spec"]]) {
        [ret appendString:@"规格/"];
    }
    if ([self isValid:dic[@"weight"]]) {
        [ret appendString:@"件重/"];
    }
    return ret;
}

- (NSString *)generateInfoVal:(NSDictionary *)dic {
    NSMutableString *ret = [[NSMutableString alloc] init];
    if ([self isValid:dic[@"product"]]) {
        [ret appendString:dic[@"product"]];
        [ret appendString:@"/"];
    }
    if ([self isValid:dic[@"variety"]]) {
        [ret appendString:dic[@"variety"]];
        [ret appendString:@"/"];
    }
    if ([self isValid:dic[@"spec"]]) {
        [ret appendString:dic[@"spec"]];
        [ret appendString:@"/"];
    }
    if ([self isValid:dic[@"weight"]]) {
        [ret appendString:dic[@"weight"]];
        [ret appendString:@"/"];
    }
    
    return ret;
}

- (Boolean)isValid:(id)val {
    if (val && ![val isKindOfClass:[NSNull class]] && ![val isEqualToString:@""]) {
        return true;
    }
    return false;
}



@end
