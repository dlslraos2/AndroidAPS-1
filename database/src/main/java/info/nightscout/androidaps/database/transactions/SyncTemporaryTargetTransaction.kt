package info.nightscout.androidaps.database.transactions

import info.nightscout.androidaps.database.entities.TemporaryTarget
import info.nightscout.androidaps.database.interfaces.end

/**
 * Sync the TemporaryTarget from NS
 */
class SyncTemporaryTargetTransaction(private val temporaryTarget: TemporaryTarget) : Transaction<Unit>() {

    override fun run() {
        if (temporaryTarget.duration != 0L) {
            // not ending event
            val current: TemporaryTarget? =
                temporaryTarget.interfaceIDs.nightscoutSystemId?.let {
                    database.temporaryTargetDao.findByNSId(it)
                }

            if (current != null) {
                // nsId exists, allow only invalidation
                if (current.isValid && !temporaryTarget.isValid) {
                    current.isValid = false
                    database.temporaryTargetDao.updateExistingEntry(current)
                    return
                }
            }

            if (current == null) {
                // new record
                val running = database.temporaryTargetDao.getTemporaryTargetActiveAt(temporaryTarget.timestamp).blockingGet()
                if (running != null) {
                    running.end = temporaryTarget.timestamp
                    database.temporaryTargetDao.updateExistingEntry(running)
                }
                database.temporaryTargetDao.insertNewEntry(temporaryTarget)
                return
            }

        } else {
            // ending event
            val running = database.temporaryTargetDao.getTemporaryTargetActiveAt(temporaryTarget.timestamp).blockingGet()
            if (running != null) {
                running.end = temporaryTarget.timestamp
                database.temporaryTargetDao.updateExistingEntry(running)
            }
        }
    }
}