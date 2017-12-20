package game

import protocol.data.Claim
import protocol.data.River
import protocol.data.Setup

enum class RiverState{ Our, Enemy1, Enemy2, Enemy3, Neutral }

class State {
    val rivers = mutableMapOf<River, RiverState>()
    var mines = listOf<Int>()
    var myId = -1

    fun init(setup: Setup) {
        myId = setup.punter
        for(river in setup.map.rivers) {
            rivers[river] = RiverState.Neutral
        }
        for(mine in setup.map.mines) {
            mines += mine
        }
    }

    fun update(claim: Claim) {
        val enemy = mutableListOf(0,1,2,3)
        enemy.remove(myId)
        rivers[River(claim.source, claim.target)] = when(claim.punter) {
            myId -> RiverState.Our
            enemy[0] -> RiverState.Enemy1
            enemy[1] -> RiverState.Enemy2
            else -> RiverState.Enemy3
        }
    }
}
