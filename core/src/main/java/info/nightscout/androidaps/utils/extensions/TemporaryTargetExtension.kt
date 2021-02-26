package info.nightscout.androidaps.utils.extensions

import info.nightscout.androidaps.Constants
import info.nightscout.androidaps.core.R
import info.nightscout.androidaps.data.Profile
import info.nightscout.androidaps.database.entities.TemporaryTarget
import info.nightscout.androidaps.utils.DecimalFormatter
import info.nightscout.androidaps.utils.JsonHelper
import info.nightscout.androidaps.utils.resources.ResourceHelper
import org.json.JSONObject
import java.util.concurrent.TimeUnit

fun TemporaryTarget.lowValueToUnitsToString(units: String): String =
    if (units == Constants.MGDL) DecimalFormatter.to0Decimal(this.lowTarget)
    else DecimalFormatter.to1Decimal(this.lowTarget * Constants.MGDL_TO_MMOLL)

fun TemporaryTarget.highValueToUnitsToString(units: String): String =
    if (units == Constants.MGDL) DecimalFormatter.to0Decimal(this.highTarget)
    else DecimalFormatter.to1Decimal(this.highTarget * Constants.MGDL_TO_MMOLL)

fun TemporaryTarget.target(): Double =
     (this.lowTarget + this.highTarget) / 2

fun TemporaryTarget.friendlyDescription(units: String, resourceHelper: ResourceHelper): String =
    Profile.toTargetRangeString(lowTarget, highTarget, Constants.MGDL, units) +
        units +
        "@" + resourceHelper.gs(R.string.format_mins, TimeUnit.MILLISECONDS.toMinutes(duration)) + "(" + reason.text + ")"

fun temporaryTargetFromJson(jsonObject: JSONObject): TemporaryTarget? {
    val units = JsonHelper.safeGetString(jsonObject, "units", Constants.MGDL)
    val timestamp = JsonHelper.safeGetLongAllowNull(jsonObject, "mills", null) ?: return null
    val duration = JsonHelper.safeGetLongAllowNull(jsonObject, "duration", null) ?: return null
    var low = JsonHelper.safeGetDoubleAllowNull(jsonObject, "targetBottom") ?: return null
    low = Profile.toMgdl(low, units)
    var high = JsonHelper.safeGetDoubleAllowNull(jsonObject, "targetTop") ?: return null
    high = Profile.toMgdl(high, units)
    val reasonString = JsonHelper.safeGetStringAllowNull(jsonObject, "reason", null) ?: return null
    val reason = TemporaryTarget.Reason.fromString(reasonString)
    val id = JsonHelper.safeGetStringAllowNull(jsonObject, "_id", null) ?: return null

    val tt = TemporaryTarget(
        timestamp = timestamp,
        duration = TimeUnit.MINUTES.toMillis(duration),
        reason = reason,
        lowTarget = low,
        highTarget = high
    )
    tt.interfaceIDs.nightscoutId = id
    return tt
}