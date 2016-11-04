//
//  BluetoothPrintManager.m
//  BluetoothPrint
//
//  Created by NovaCloud on 16/11/4.
//  Copyright © 2016年 NovaCloud. All rights reserved.
//

#import "BluetoothPrintViewManager.h"
#import "RCTViewManager.h"
#import "BluetoothPrinter.h"
#import "BluetoothPrinterListView.h"

@interface BluetoothPrintViewManager()

@property (nonatomic, strong) BluetoothPrinterListView *deviceListView;
@property (nonnull, strong) BluetoothPrinter *printer;

@end

@implementation BluetoothPrintViewManager

RCT_EXPORT_MODULE();

RCT_EXPORT_VIEW_PROPERTY(pitchEnabled, BOOL);


- (BluetoothPrinterListView *)deviceListView {
    if (!_deviceListView) {
        _deviceListView = [[BluetoothPrinterListView alloc] init];
    } else {
        [_deviceListView startScan];
    }
    return _deviceListView;
}

- (UIView *)view {
    return self.deviceListView;
}

@end
