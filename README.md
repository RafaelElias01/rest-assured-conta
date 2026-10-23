# Como testar este projeto

O arquivo `src/test/java/org/acme/ContaBancariaResourceTest.java` tem 9 testes de Rest Assured para a API de conta bancária. Aqui está como rodar e como ler cada um.

## 1. Rodar

No terminal, dentro da pasta do projeto:

```
mvn test
```

- O Quarkus sobe a aplicação sozinho na porta **8081** e roda os testes contra ela. Não precisa subir nada antes.
- No final aparece `Tests run: 9, Failures: 0`. Isso significa que passou.
- Para rodar só uma classe: `mvn test -Dtest=ContaBancariaResourceTest`.
- No VS Code, também dá para clicar no ▶ ao lado de cada `@Test`, se tiver a extensão de Java.

**Teste manual (opcional):** `mvn quarkus:dev` sobe na porta 8080. Depois abra `http://localhost:8080/conta/deposito?saldo=100&valor=50` no navegador e veja o JSON.

## 2. A anatomia de um teste: given / when / then

Todo teste de Rest Assured tem três partes. Exemplo:

```java
given()                              // PREPARO: o que vou enviar
    .queryParam("saldo", 100)        //   ?saldo=100
    .queryParam("valor", 50)         //   &valor=50
.when()                              // AÇÃO: qual chamada faço
    .get("/conta/deposito")          //   GET /conta/deposito
.then()                              // VERIFICAÇÃO: o que espero de volta
    .statusCode(200)                 //   HTTP 200 (OK)
    .body("resultado", equalTo(150.0f));  // no JSON, campo "resultado" = 150
```

Ou seja: "dado isto, quando eu fizer aquilo, então espero isto".

## 3. O que cada exercício ensina

| Exercício | O que você aprende |
|---|---|
| 1 | `queryParam` e `statusCode(200)` |
| 2 | Erro de negócio: espere `400` e leia a `mensagem` do JSON de erro |
| 3 | `POST` com JSON: `contentType` + `body(...)` |
| 4 | Path param: o valor vai direto na URL (`/rendimento/5`) |
| 5 | Header `Authorization`. Sem ele, espere `401` |
| 6 | Encadear: `.extract().path("resultado")` guarda o valor para usar na próxima chamada |

## 4. Armadilhas comuns

- **`equalTo(150.0f)` e não `equalTo(150)`.** O Rest Assured lê números do JSON como `float`. Com `int` o teste falha, mesmo parecendo igual.
- **O caminho do `body("resultado", ...)`** é o nome do campo no JSON. Se o record `Resultado` tiver outro nome de campo, o teste quebra.
- **`greaterThan`** serve quando você não quer comparar o valor exato (exercício 3).
- **Importe os métodos estáticos** (`given`, `equalTo`...), senão o código não compila.

## 5. Como aprender de verdade: quebre de propósito

1. Mude `equalTo(150.0f)` para `equalTo(999.0f)` e rode. Leia a mensagem de erro: ela mostra o esperado e o recebido.
2. Troque `statusCode(200)` por `statusCode(404)`.
3. Apague o header `Authorization` do `extratoComTokenDeveDescontarATaxa` e veja virar 401.
4. Escreva um teste novo sozinho. Sugestão: `rendimentoDe0MesesDeveSer1000`. Veja primeiro o que o endpoint devolve.

Para depurar qualquer teste, coloque `.log().all()` logo depois do `given()` (mostra o que foi enviado) e logo depois do `.then()` (mostra a resposta completa).
