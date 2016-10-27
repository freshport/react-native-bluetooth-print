import React, { Component, PropTypes } from 'react'
import { requireNativeComponent, NativeModules } from 'react-native'

class BlueToothPrinterList extends Component {
    constructor(props) {
        super(props)
    }
    render() {
        const DeviceList = requireNativeComponent('BlueToothPrint', DeviceList)
        return <DeviceList {...this.props} />
    }
}


export default class BlueToothPrint {
    static orderPrint(array) {
        NativeModules.BlueToothPrint.orderPrint(array)
    }
    static hasConnectedToAPrinter() {
        const promise = new Promise((resolve, reject) => {
            NativeModules.BlueToothPrint.hasConnectedToAPrinter((err, ret) => {
                err ? reject(err) : resolve(ret)
            })
        })
        return promise
    }
    static get BlueToothPrinterListView() {
        return BlueToothPrinterList
    }
}



