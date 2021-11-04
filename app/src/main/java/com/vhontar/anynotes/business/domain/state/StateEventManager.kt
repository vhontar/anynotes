import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vhontar.anynotes.business.domain.state.StateEvent

/**
 * - Keeps track of active StateEvents in DataChannelManager
 * - Keeps track of whether the progress bar should show or not based on a boolean
 *      value in each StateEvent (shouldDisplayProgressBar)
 */
class StateEventManager {

    private val activeStateEvents: HashMap<String, StateEvent> = HashMap()

    private val _shouldDisplayProgressBar = MutableLiveData<Boolean>()
    val shouldDisplayProgressBar: LiveData<Boolean> = _shouldDisplayProgressBar

    fun getActiveJobNames(): MutableSet<String> {
        return activeStateEvents.keys
    }

    fun clearActiveStateEventCounter() {
        activeStateEvents.clear()
        syncNumActiveStateEvents()
    }

    fun addStateEvent(stateEvent: StateEvent) {
        activeStateEvents[stateEvent.eventName()] = stateEvent
        syncNumActiveStateEvents()
    }

    fun removeStateEvent(stateEvent: StateEvent?) {
        activeStateEvents.remove(stateEvent?.eventName())
        syncNumActiveStateEvents()
    }

    fun isStateEventActive(stateEvent: StateEvent): Boolean {
        for (eventName in activeStateEvents.keys) {
            if (stateEvent.eventName() == eventName) {
                return true
            }
        }
        return false
    }

    private fun syncNumActiveStateEvents() {
        var shouldDisplayProgressBar = false
        for (stateEvent in activeStateEvents.values) {
            if (stateEvent.shouldDisplayProgressBar()) {
                shouldDisplayProgressBar = true
            }
        }
        _shouldDisplayProgressBar.value = shouldDisplayProgressBar
    }
}