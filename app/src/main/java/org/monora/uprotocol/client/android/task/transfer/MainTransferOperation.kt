package org.monora.uprotocol.client.android.task.transfer

import org.monora.uprotocol.client.android.R
import org.monora.uprotocol.client.android.io.DocumentFileStreamDescriptor
import org.monora.uprotocol.client.android.io.FileStreamDescriptor
import org.monora.uprotocol.client.android.service.backgroundservice.TaskMessage
import org.monora.uprotocol.client.android.service.backgroundservice.TaskStoppedException
import org.monora.uprotocol.client.android.task.FileTransferTask
import org.monora.uprotocol.client.android.util.Files
import org.monora.uprotocol.core.io.StreamDescriptor
import org.monora.uprotocol.core.transfer.TransferItem
import org.monora.uprotocol.core.transfer.TransferOperation

class MainTransferOperation(val task: FileTransferTask) : TransferOperation {
    private var ongoing: TransferItem? = null

    private var bytesOngoing: Long = 0

    private var bytesTotal: Long = 0

    private var count = 0

    override fun clearBytesOngoing() {
        bytesOngoing = 0
    }

    override fun clearOngoing() {
        ongoing = null
    }

    override fun finishOperation() {
        if (count > 0) {
            task.notificationHelper.notifyFileReceived(task, Files.getSavePath(task.context, task.transfer))
        }
    }

    override fun getBytesOngoing(): Long = bytesOngoing

    override fun getBytesTotal(): Long = bytesTotal

    override fun getCount(): Int = count

    override fun getOngoing(): TransferItem? = ongoing

    override fun installReceivedContent(descriptor: StreamDescriptor) {
        if (descriptor is DocumentFileStreamDescriptor) {

        }
    }

    override fun onCancelOperation() {
        TODO("Not yet implemented")
    }

    override fun onUnhandledException(e: Exception) {
        try {
            task.post(
                TaskMessage.newInstance(
                    task.context.getString(R.string.text_communicationError),
                    task.context.getString(R.string.mesg_errorDuringTransfer, task.client.clientNickname),
                    TaskMessage.Tone.Negative
                )
            )
        } catch (ignored: TaskStoppedException) {
        }
    }

    override fun publishProgress() {
        task.publishStatus()
    }

    override fun setBytesOngoing(bytes: Long, bytesIncrease: Long) {
        bytesOngoing = bytes
    }

    override fun setBytesTotal(bytes: Long) {
        bytesTotal = bytes
    }

    override fun setCount(count: Int) {
        this.count = count
    }

    override fun setOngoing(transferItem: TransferItem) {
        ongoing = transferItem
    }
}