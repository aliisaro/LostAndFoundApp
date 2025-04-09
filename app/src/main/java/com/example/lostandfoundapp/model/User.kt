package com.example.lostandfoundapp.model

data class User(
    val email: String = "",
    //profile pic?
    //username?
)

//jos halutaan että käyttäjällä voi olla username ja pfp niin pitää tehä funktio,
// joka lisää käyttäjätiedot tietokantaan "users" tauluun