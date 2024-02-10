package com.example.takequiz

data class QuizModel(
    val roomNo:Int,
    val roomID:String,
    val topic:String,
    var providedQuestions:String,
    val duration:String
)

data class Questions(
    var root:String,
    var questions: String,
    var correctAns: String,
    var op1: String,
    var op2: String,
    var op3: String,
    var op4: String,
)