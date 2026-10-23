package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ContaBancariaResourceTest {

    // Exercício 1: status code e query params
    @Test
    void depositoDeveSomarOValor() {
        given()
            .queryParam("saldo", 100)
            .queryParam("valor", 50)
        .when()
            .get("/conta/deposito")
        .then()
            .statusCode(200)
            .body("resultado", equalTo(150.0f));
    }

    // Exercício 2: erro de negócio
    @Test
    void saqueComSaldoInsuficienteDeveRetornar400() {
        given()
            .queryParam("saldo", 100)
            .queryParam("valor", 150)
        .when()
            .get("/conta/saque")
        .then()
            .statusCode(400)
            .body("mensagem", equalTo("Saldo insuficiente"));
    }

    @Test
    void saqueComSaldoSuficienteDeveDescontarOValor() {
        given()
            .queryParam("saldo", 100)
            .queryParam("valor", 40)
        .when()
            .get("/conta/saque")
        .then()
            .statusCode(200)
            .body("resultado", equalTo(60.0f));
    }

    // Exercício 3: JSON no corpo (POST)
    @Test
    void faturaDeveSomarAsCompras() {
        given()
            .contentType("application/json")
            .body("[20.0, 35.5, 10.0]")
        .when()
            .post("/conta/fatura")
        .then()
            .statusCode(200)
            .body("resultado", greaterThan(60.0f));
    }

    @Test
    void faturaComListaVaziaDeveRetornar400() {
        given()
            .contentType("application/json")
            .body("[]")
        .when()
            .post("/conta/fatura")
        .then()
            .statusCode(400);
    }

    // Exercício 4: path param
    @Test
    void rendimentoDe5MesesDeveSer1050() {
        given()
        .when()
            .get("/conta/rendimento/5")
        .then()
            .statusCode(200)
            .body("resultado", equalTo(1050.0f));
    }

    // Exercício 5: autenticação obrigatória
    @Test
    void extratoComTokenDeveDescontarATaxa() {
        given()
            .header("Authorization", "Bearer abc.123.xyz")
            .queryParam("saldo", 500)
        .when()
            .get("/conta/extrato")
        .then()
            .statusCode(200)
            .body("resultado", equalTo(490.0f));
    }

    @Test
    void extratoSemTokenDeveRetornar401() {
        given()
            .queryParam("saldo", 500)
        .when()
            .get("/conta/extrato")
        .then()
            .statusCode(401);
    }

    // Exercício 6: desafio, usando o resultado de um endpoint no outro
    @Test
    void depositoEExtratoEmSequencia() {
        float saldoNovo = given()
            .queryParam("saldo", 100)
            .queryParam("valor", 400)
        .when()
            .get("/conta/deposito")
        .then()
            .statusCode(200)
            .body("resultado", equalTo(500.0f))
            .extract().path("resultado");

        given()
            .header("Authorization", "Bearer abc.123.xyz")
            .queryParam("saldo", saldoNovo)
        .when()
            .get("/conta/extrato")
        .then()
            .statusCode(200)
            .body("resultado", equalTo(490.0f));
    }
}
