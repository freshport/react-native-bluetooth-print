import React, { Component, PropTypes } from 'react'
import {
    requireNativeComponent,
    NativeModules,
    Platform,
    View
} from 'react-native'


let PrinterList
if (Platform.OS === 'ios') {
    PrinterList = requireNativeComponent('BluetoothPrintView', null)
} else if (Platform.OS === 'android') {
    const iface = {
        name: 'PrinterList',
        propTypes: {
            ...View.propTypes
        }
    }
    PrinterList = requireNativeComponent('BluetoothPrintView', iface)
}

class BluetoothPrinterList extends Component {
    constructor(props) {
        super(props)
    }
    render() {
        return <PrinterList {...this.props } />
    }
}

export default class BluetoothPrint {
    static orderPrint(array) {
        NativeModules.BluetoothPrint.orderPrint(array)
    }
    static setDelay(delay) {
        NativeModules.BluetoothPrint.setDelay(delay)
    }
    static hasConnectedToAPrinter() {
        const promise = new Promise((resolve, reject) => {
            NativeModules.BluetoothPrint.hasConnectedToAPrinter((err, ret) => {
                err ? reject(err) : resolve(ret)
            })
        })
        return promise
    }
    static get BluetoothPrinterListView() {
        return BluetoothPrinterList
    }
    static connectedDeviceName() {
        const promise = new Promise((resolve, reject) => {
            NativeModules.BluetoothPrint.connectedDeviceName((err, ret) => {
                err ? reject(err) : resolve(ret)
            })
        })
	return promise
    }
}
