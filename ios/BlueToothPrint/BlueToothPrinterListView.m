//
//  BlueToothPrinterListView.m
//  BlueToothPrint
//
//  Created by euky on 2016/10/21.
//  Copyright © 2016年 euky. All rights reserved.
//

#import "BlueToothPrinterListView.h"
#import "BlueToothPrinter.h"

@interface BlueToothPrinterListView()<UITableViewDelegate, UITableViewDataSource, UartDelegate>

@property (nonatomic, strong) BlueToothPrinter *printer;
@property (nonatomic, strong) NSMutableArray *deviceList;

@end

@implementation BlueToothPrinterListView

- (instancetype) init {
    self = [super initWithFrame:CGRectZero style:UITableViewStyleGrouped];
    if (self) {
        self.delegate = self;
        self.dataSource = self;
        self.tableFooterView = [[UIView alloc] initWithFrame:CGRectZero];
        self.tableHeaderView = [[UIView alloc] initWithFrame:CGRectZero];
        [self.printer scanStart];
    }
    return self;
}

- (void)startScan {
    [self.printer scanStart];
}

- (BlueToothPrinter *)printer {
    if (!_printer) {
        _printer = [BlueToothPrinter sharedInstance];
        [_printer setUartDelegate:self];
    }
    return _printer;
}

- (NSMutableArray *)deviceList {
    if (!_deviceList) {
        _deviceList = [[NSMutableArray alloc] init];
        CBPeripheral *connectedPer = self.printer.retrieveConnectedPeripherals.firstObject;
        _deviceList[0] = [[NSMutableArray alloc] init];
        _deviceList[1] = [[NSMutableArray alloc] init];
        
        if (connectedPer) {
            [_deviceList[0] addObject:connectedPer];
        }
    }
    return _deviceList;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.deviceList[section] count];
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return self.deviceList.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellID;
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellID];
    if (!cell) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleValue1 reuseIdentifier:cellID];
    }
    CBPeripheral *per = self.deviceList[indexPath.section][indexPath.row];
    cell.accessoryType = indexPath.section == 0 ? UITableViewCellAccessoryCheckmark : UITableViewCellAccessoryNone;
    cell.textLabel.text = per.name;
    
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [self.printer scanStop];
    CBPeripheral *per = self.deviceList[indexPath.section][indexPath.row];
    if (per) {
        [self.printer connectPeripheral:per];
    }
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section {
    if (section == 0 && [self.deviceList[section] count] > 0) {
        return @"已连接的设备";
    } else if (section == 1) {
        return @"选择一个设备";
    }
    return nil;
}

- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section {
    return 0.1f;
}

- (CGFloat)tableView:(UITableView *)tableView heightForHeaderInSection:(NSInteger)section {
    return 31.f;
}

- (void)didScanedPeripherals:(NSMutableArray *)foundPeripherals {
    self.deviceList[1] = foundPeripherals;
    [self reloadData];
}

- (void)didConnectPeripheral:(CBPeripheral *)peripheral {
    [self.deviceList[0] addObject:peripheral];
    [self.deviceList[1] removeObject:peripheral];
    [self reloadData];
}

- (void)didDiscoverPeripheral:(CBPeripheral *)peripheral RSSI:(NSNumber *)RSSI{
    
}

- (void) didDiscoverPeripheralAndName:(CBPeripheral *)peripheral DevName:(NSString *)devName{
    
}
- (void) didBluetoothPoweredOff {
    
}
- (void)didRecvRSSI:(CBPeripheral *)peripheral RSSI:(NSNumber *)RSSI {
    
}
- (void)didReceiveData:(CBPeripheral *)peripheral recvData:(NSData *)recvData {
    
}
- (void)didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    
}
- (void)didBluetoothPoweredOn {
    
}
- (void)didWriteData:(CBPeripheral *)peripheral error:(NSError *)error {
    
}
- (void)didrecvCustom:(CBPeripheral *)peripheral CustomerRight:(bool)bRight {
    
}
- (void)didRetrievePeripheral:(NSArray *)peripherals {
    
}
/*
 // Only override drawRect: if you perform custom drawing.
 // An empty implementation adversely affects performance during animation.
 - (void)drawRect:(CGRect)rect {
 // Drawing code
 }
 */

@end
