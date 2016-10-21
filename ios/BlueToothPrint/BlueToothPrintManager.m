//
//  BlueToothPrint.m
//  BlueToothPrint
//
//  Created by euky on 2016/10/20.
//  Copyright © 2016年 euky. All rights reserved.
//

#import "BlueToothPrintManager.h"
#import "RCTViewManager.h"
#import "BlueToothPrinter.h"
#import "BlueToothPrinterListView.h"

@interface BlueToothPrintManager()

@property (nonatomic, strong) BlueToothPrinterListView *deviceListView;
@property (nonnull, strong) BlueToothPrinter *printer;

@end

@implementation BlueToothPrintManager

RCT_EXPORT_MODULE();

RCT_EXPORT_VIEW_PROPERTY(pitchEnabled, BOOL);


- (BlueToothPrinterListView *)deviceListView {
    if (!_deviceListView) {
        _deviceListView = [[BlueToothPrinterListView alloc] init];
    } else {
         [_deviceListView startScan];
    }
    return _deviceListView;
}

- (UIView *)view {
    return self.deviceListView;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 10;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellID;
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellID];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:cellID];
    }
    cell.textLabel.text = @"text";
    return cell;
}

@end
