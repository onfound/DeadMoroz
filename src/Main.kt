import game.Brain
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import game.State
import protocol.Protocol
import protocol.data.*

object Arguments {
    @Option(name = "-u", usage = "Specify server url")
    var url: String = ""

    @Option(name = "-p", usage = "Specify server port")
    var port: Int = -1

    fun use(args: Array<String>): Arguments =
            CmdLineParser(this).parseArgument(*args).let { this }
}

fun main(args: Array<String>) {
    Arguments.use(args)

    println("Hi, Dead Moroz")

    // Протокол обмена с сервером
    val protocol = Protocol(Arguments.url, Arguments.port)
    // Состояние игрового поля
    val gameState = State()
    // Джо очень умный чувак, вот его ум
    val brain = Brain(gameState, protocol)

    protocol.handShake("Dead Moroz")
    val setupData = protocol.setup()
    gameState.init(setupData)

    println("Received id = ${setupData.punter}")

    protocol.ready()

    gameloop@ while (true) {
        val message = protocol.serverMessage()
        when (message) {
            is GameResult -> {
                println("The game is over!")
                val myScore = message.stop.scores[protocol.myId]
                println("DeadMoroz scored ${myScore.score} points!")
                break@gameloop
            }
            is Timeout -> {
                println("(￣o￣) zzZZzzZZ")
            }
            is GameTurnMessage -> {
                for (move in message.move.moves) {
                    when (move) {
                        is PassMove -> {
                        }
                        is ClaimMove -> {
                            gameState.update(move.claim)
                        }
                    }
                }
            }
        }
        val time = System.currentTimeMillis()
        brain.makeMove()
        println("TOTAL TIME = " + (System.currentTimeMillis() - time))
    }
}
