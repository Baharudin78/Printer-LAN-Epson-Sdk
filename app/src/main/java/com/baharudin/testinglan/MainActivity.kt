package com.baharudin.testinglan

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.baharudin.testinglan.databinding.ActivityMainBinding
import com.baharudin.testinglan.utility.PrinterUtils
import com.baharudin.testinglan.utility.ProgressIndicator
import com.baharudin.testinglan.utility.ShowMsg
import com.epson.epos2.Epos2Exception
import com.epson.epos2.Log
import com.epson.epos2.printer.Printer
import com.epson.epos2.printer.PrinterStatusInfo
import com.epson.epos2.printer.ReceiveListener

class MainActivity : Activity(), View.OnClickListener, ReceiveListener {
    private lateinit var binding : ActivityMainBinding
    private var mContext: Context? = null
    private lateinit var mProgressIndicator : ProgressIndicator
    private var mPrinter: Printer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mContext = this
        mProgressIndicator = ProgressIndicator(mContext)

        initializeObject()

        try {
            Log.setLogSettings(
                mContext,
                Log.PERIOD_TEMPORARY,
                Log.OUTPUT_STORAGE,
                null,
                0,
                50,
                Log.LOGLEVEL_LOW
            )
        } catch (e: Exception) {
            ShowMsg.showException(e, "setLogSettings", this)
        }

        binding.btnSampleReceipt.setOnClickListener {
            mProgressIndicator.beginProgress(getString(R.string.progress_msg))
            Thread {
                if (!runPrintReceiptSequence()) {
                    mProgressIndicator.endProgress()
                }
            }.start()
        }
    }

    override fun onDestroy() {
        finalizeObject()
        super.onDestroy()
    }

    override fun onClick(v: View) {

    }

    private fun runPrintReceiptSequence(): Boolean {
        if (!createReceiptData()) {
            return false
        }
        if (!printData()){
            return false
        }
        return true
    }

    private fun createReceiptData(): Boolean {
        var method = ""
        val logoData = BitmapFactory.decodeResource(resources, R.drawable.hitachihhaahah)
        var textData: StringBuilder? = StringBuilder()
        val barcodeWidth = 2
        val barcodeHeight = 100
        if (mPrinter == null) {
            return false
        }
        try {
            method = "addTextAlign"
            mPrinter?.addTextAlign(Printer.ALIGN_CENTER)
            method = "addImage"
            PrinterUtils.addImage(mPrinter, logoData)
            method = "addFeedLine"
            mPrinter?.addFeedLine(1)
            textData?.append("THE STORE 123 (555) 555 – 5555\n")
            textData?.append("STORE DIRECTOR – John Smith\n")
            textData?.append("\n")
            textData?.append("7/01/07 16:58 6153 05 0191 134\n")
            textData?.append("ST# 21 OP# 001 TE# 01 TR# 747\n")
            textData?.append("------------------------------\n")
            method = "addText"
            mPrinter?.addText(textData.toString())
            textData?.delete(0, textData.length)
            textData?.append("400 OHEIDA 3PK SPRINGF  9.99 R\n")
            textData?.append("410 3 CUP BLK TEAPOT    9.99 R\n")
            textData?.append("445 EMERIL GRIDDLE/PAN 17.99 R\n")
            textData?.append("438 CANDYMAKER ASSORT   4.99 R\n")
            textData?.append("474 TRIPOD              8.99 R\n")
            textData?.append("433 BLK LOGO PRNTED ZO  7.99 R\n")
            textData?.append("458 AQUA MICROTERRY SC  6.99 R\n")
            textData?.append("493 30L BLK FF DRESS   16.99 R\n")
            textData?.append("407 LEVITATING DESKTOP  7.99 R\n")
            textData?.append("441 **Blue Overprint P  2.99 R\n")
            textData?.append("476 REPOSE 4PCPM CHOC   5.49 R\n")
            textData?.append("461 WESTGATE BLACK 25  59.99 R\n")
            textData?.append("------------------------------\n")
            method = "addText"
            mPrinter?.addText(textData.toString())
            textData?.delete(0, textData.length)
            textData?.append("SUBTOTAL                160.38\n")
            textData?.append("TAX                      14.43\n")
            method = "addText"
            mPrinter?.addText(textData.toString())
            textData?.delete(0, textData.length)
            method = "addTextSize"
            mPrinter?.addTextSize(2, 2)
            method = "addText"
            mPrinter?.addText("TOTAL    174.81\n")
            method = "addTextSize"
            mPrinter?.addTextSize(1, 1)
            method = "addFeedLine"
            mPrinter?.addFeedLine(1)
            textData?.append("CASH                    200.00\n")
            textData?.append("CHANGE                   25.19\n")
            textData?.append("------------------------------\n")
            method = "addText"
            mPrinter?.addText(textData.toString())
            textData?.delete(0, textData.length)
            textData?.append("Purchased item total number\n")
            textData?.append("Sign Up and Save !\n")
            textData?.append("With Preferred Saving Card\n")
            method = "addText"
            mPrinter?.addText(textData.toString())
            textData?.delete(0, textData.length)
            method = "addFeedLine"
            mPrinter?.addFeedLine(2)
            method = "addBarcode"
            mPrinter?.addBarcode(
                "01209457",
                Printer.BARCODE_CODE39,
                Printer.HRI_BELOW,
                Printer.FONT_A,
                barcodeWidth,
                barcodeHeight
            )
            method = "addCut"
            mPrinter?.addCut(Printer.CUT_FEED)
        } catch (e: Exception) {
            mPrinter?.clearCommandBuffer()
            ShowMsg.showException(e, method, this)
            return false
        }
        textData = null
        return true
    }

    private fun printData(): Boolean {
        if (mPrinter == null) {
            return false
        }
        if (!connectPrinter()) {
            mPrinter?.clearCommandBuffer()
            return false
        }
        try {
            mPrinter?.sendData(Printer.PARAM_DEFAULT)
        } catch (e: Exception) {
            mPrinter?.clearCommandBuffer()
            ShowMsg.showException(e, "sendData", this)
            try {
                mPrinter?.disconnect()
            } catch (ex: Exception) {
                ShowMsg.showException(ex, "Error", this)
            }
            return false
        }
        return true
    }

    private fun initializeObject(): Boolean {
        try {
            mPrinter = Printer(
                Printer.TM_T82,
                Printer.MODEL_ANK,
                mContext
            )
        } catch (e: Exception) {
            ShowMsg.showException(e, "Printer", this)
            return false
        }
        mPrinter?.setReceiveEventListener(this)
        return true
    }

    private fun finalizeObject() {
        if (mPrinter == null) {
            return
        }
        mPrinter?.setReceiveEventListener(null)
        mPrinter = null
    }

    private fun connectPrinter(): Boolean {
        if (mPrinter == null) {
            return false
        }
        try {
            mPrinter?.connect("TCP:172.16.2.29", Printer.PARAM_DEFAULT)
        } catch (e: Exception) {
            ShowMsg.showException(e, "connect ${e.message}", this)
            return false
        }
        return true
    }

    private fun disconnectPrinter() {
        if (mPrinter == null) {
            return
        }
        while (true) {
            try {
                mPrinter?.disconnect()
                break
            } catch (e: Exception) {
                if (e is Epos2Exception) {
                    if (e.errorStatus == Epos2Exception.ERR_PROCESSING) {
                        try {
                            Thread.sleep(DISCONNECT_INTERVAL.toLong())
                        } catch (ex: Exception) {
                        }
                    } else {
                        runOnUiThread { ShowMsg.showException(e, "disconnect", this) }
                        break
                    }
                } else {
                    runOnUiThread { ShowMsg.showException(e, "disconnect", this) }
                    break
                }
            }
        }
        mPrinter?.clearCommandBuffer()
    }

    private fun makeErrorMessage(status: PrinterStatusInfo): String {
        var msg = ""
        if (status.online == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_offline)
        }
        if (status.connection == Printer.FALSE) {
            msg += getString(R.string.handlingmsg_err_no_response)
        }
        if (status.coverOpen == Printer.TRUE) {
            msg += getString(R.string.handlingmsg_err_cover_open)
        }
        if (status.paper == Printer.PAPER_EMPTY) {
            msg += getString(R.string.handlingmsg_err_receipt_end)
        }
        if (status.paperFeed == Printer.TRUE || status.panelSwitch == Printer.SWITCH_ON) {
            msg += getString(R.string.handlingmsg_err_paper_feed)
        }
        if (status.errorStatus == Printer.MECHANICAL_ERR || status.errorStatus == Printer.AUTOCUTTER_ERR) {
            msg += getString(R.string.handlingmsg_err_autocutter)
            msg += getString(R.string.handlingmsg_err_need_recover)
        }
        if (status.errorStatus == Printer.UNRECOVER_ERR) {
            msg += getString(R.string.handlingmsg_err_unrecover)
        }
        if (status.errorStatus == Printer.AUTORECOVER_ERR) {
            if (status.autoRecoverError == Printer.HEAD_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat)
                msg += getString(R.string.handlingmsg_err_head)
            }
            if (status.autoRecoverError == Printer.MOTOR_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat)
                msg += getString(R.string.handlingmsg_err_motor)
            }
            if (status.autoRecoverError == Printer.BATTERY_OVERHEAT) {
                msg += getString(R.string.handlingmsg_err_overheat)
                msg += getString(R.string.handlingmsg_err_battery)
            }
            if (status.autoRecoverError == Printer.WRONG_PAPER) {
                msg += getString(R.string.handlingmsg_err_wrong_paper)
            }
        }
        if (status.batteryLevel == Printer.BATTERY_LEVEL_0) {
            msg += getString(R.string.handlingmsg_err_battery_real_end)
        }
        if (status.removalWaiting == Printer.REMOVAL_WAIT_PAPER) {
            msg += getString(R.string.handlingmsg_err_wait_removal)
        }
        if (status.unrecoverError == Printer.HIGH_VOLTAGE_ERR ||
            status.unrecoverError == Printer.LOW_VOLTAGE_ERR
        ) {
            msg += getString(R.string.handlingmsg_err_voltage)
        }
        return msg
    }

    private fun dispPrinterWarnings(status: PrinterStatusInfo?) {
        var warningsMsg = ""
        if (status == null) {
            return
        }
        if (status.paper == Printer.PAPER_NEAR_END) {
            warningsMsg += getString(R.string.handlingmsg_warn_receipt_near_end)
        }
        if (status.batteryLevel == Printer.BATTERY_LEVEL_1) {
            warningsMsg += getString(R.string.handlingmsg_warn_battery_near_end)
        }
        if (status.paperTakenSensor == Printer.REMOVAL_DETECT_PAPER) {
            warningsMsg += getString(R.string.handlingmsg_warn_detect_paper)
        }
        if (status.paperTakenSensor == Printer.REMOVAL_DETECT_UNKNOWN) {
            warningsMsg += getString(R.string.handlingmsg_warn_detect_unknown)
        }
        binding.tvWarning.text = warningsMsg
    }

    override fun onPtrReceive(
        printerObj: Printer,
        code: Int,
        status: PrinterStatusInfo,
        printJobId: String?
    ) {
        runOnUiThread {
            Thread { disconnectPrinter() }.start()
            mProgressIndicator.endProgress()
            ShowMsg.showResult(code, makeErrorMessage(status), this)
            dispPrinterWarnings(status)
        }
    }

    companion object {
        private const val REQUEST_PERMISSION = 100
        private const val DISCONNECT_INTERVAL = 500
    }
}