import com.mongodb.util.JSON
import io.javalin.Javalin
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection
import kotlinx.serialization.*
import kotlinx.serialization.json.*
@Serializable
data class Todo( val item:String?, val value:String?)

fun main(args: Array<String>) {
    println("What is your port?")
    var port= readLine()
    if(port =="")port ="8080"
    val client=KMongo.createClient()
    val todos=client.getDatabase("TODO")
    val collection=todos.getCollection<Todo>()
    val app = Javalin.create().start(port?.toInt()?.or(8080) ?: 8080)
    app.before{ctx->
        ctx.res.addHeader("Server", "Kotlin")
        ctx.res.addHeader("X-origin-server", "Kotlin")}
    app.error(404){
        ctx->
        ctx.res.addHeader("Content-Type", "application/json")
        ctx.result("{\"statusCode\":404, \"message\":\"Not Found\"}")
    }
    app.get("/"){
            ctx->
        ctx.res.addHeader("Content-Type", "application/json")
        ctx.result("{\"message\":\"Kotlin Todo App\"}")
    }
    app.post("/todos"){
        ctx->
        ctx.res.addHeader("Accept-Content", "application/json")
        if(ctx.body() == "") {
            ctx.status(400)
             ctx.result("No Body")
            return@post
        }
        ctx.bodyValidator<Todo>().check({it.value !== null&&it.item !== null}, "VALUE AND ITEM cannot be NON").get()
        var data:Todo
        try{
        data=ctx.bodyAsClass<Todo>()}
        catch(err:Error){
            ctx.status(400)
            ctx.result("BODY malformed")
            return@post
        }

        collection.insertOne(data)
        ctx.status(201)
        ctx.result("OK")
    }
}


