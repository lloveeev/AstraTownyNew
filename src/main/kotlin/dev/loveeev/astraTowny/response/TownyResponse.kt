package dev.loveeev.astratowny.response

import dev.loveeev.astratowny.events.response.ResponseFailEvent
import dev.loveeev.astratowny.events.response.ResponseSuccessEvent
import dev.loveeev.utils.BukkitUtils

data class TownyResponse(val status: Status, val message: String) {

    val isSuccess: Boolean get() = status == Status.SUCCESS

    companion object {
        fun success(message: String): TownyResponse {
            BukkitUtils.fireEvent(ResponseSuccessEvent(message))
            return TownyResponse(Status.SUCCESS, message)
        }

        fun failure(message: String): TownyResponse {
            BukkitUtils.fireEvent(ResponseFailEvent(message))
            return TownyResponse(Status.FAILURE, message)
        }
    }
}

enum class Status {
    SUCCESS,
    FAILURE
}
