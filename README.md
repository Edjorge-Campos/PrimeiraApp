# PrimeiraApp

Aplicação Android para consultar turnos, gerir disponibilidade semanal e
inscrever utilizadores em turnos. O projeto foi criado como uma aplicação de
aprendizagem, com foco em Android, autenticação e persistência de dados.

## Funcionalidades

- Autenticação com email e password através do Firebase Authentication.
- Sessão persistente e logout.
- Consulta de turnos abertos por dia da semana.
- Inscrição e cancelamento de inscrição num turno.
- Persistência da disponibilidade semanal do utilizador.
- Perfil com nome e número de telefone.
- Regras Firestore para limitar o acesso aos dados do próprio utilizador.
- Gestão de turnos reservada a utilizadores com o custom claim `admin: true`.

## Tecnologias

- Java
- Android SDK
- XML layouts
- Material Components
- Firebase Authentication
- Cloud Firestore
- Gradle

## Estrutura dos dados

Os turnos são guardados na coleção `turnos`:

```text
turnos/{shiftId}
```

Cada turno deve conter:

```json
{
  "DiaDaSemana": 1,
  "Inicio": "08:00",
  "Status": "open"
}
```

As inscrições ficam associadas ao utilizador:

```text
turnos/{shiftId}/signups/{uid}
```

Os perfis ficam guardados em:

```text
Users/{uid}
```

Mais detalhes sobre o modelo Firebase estão em
[docs/firebase-shifts.md](docs/firebase-shifts.md).

## Configuração local

### Requisitos

- Android Studio
- JDK compatível com a versão do Android Gradle Plugin usada no projeto
- Uma conta Firebase
- Firebase CLI, apenas para publicar as Firestore Rules

### Firebase

1. Cria ou seleciona um projeto Firebase.
2. Adiciona uma aplicação Android com o package name
   `com.example.primeiraapp`.
3. Ativa **Authentication > Email/Password**.
4. Cria uma base de dados Cloud Firestore.
5. Descarrega o ficheiro `google-services.json` para:

   ```text
   app/google-services.json
   ```

6. Publica as regras:

   ```bash
   firebase login
   firebase use <project-id>
   firebase deploy --only firestore:rules
   ```

O ficheiro `google-services.json` é específico do projeto Firebase local e não
deve ser commitado. Também nunca devem ser adicionadas credenciais do Firebase
Admin ou service accounts ao repositório.

### Admin

A criação, edição e remoção de turnos exige o custom claim:

```json
{
  "admin": true
}
```

Este claim deve ser atribuído apenas a partir de um ambiente confiável usando o
Firebase Admin SDK. Depois de alterar o claim, o utilizador deve terminar e
iniciar sessão novamente para atualizar o token.

## Executar a aplicação

1. Abre o projeto pela pasta raiz no Android Studio.
2. Coloca o `google-services.json` em `app/google-services.json`.
3. Sincroniza o projeto com o Gradle.
4. Executa a aplicação num emulador ou dispositivo Android.

Para validar o wrapper Gradle:

```bash
./gradlew --version
```

## Testes

Os testes locais estão em:

```text
app/src/test
app/src/androidTest
```

Os testes Firebase e das Firestore Rules devem ser executados num ambiente
Firebase configurado. A aplicação ainda não inclui uma suite completa de
integração com o Firebase Emulator Suite.

## Segurança

As regras em `firestore.rules` garantem, entre outras coisas, que:

- apenas admins podem alterar turnos;
- um utilizador só pode gerir o próprio perfil;
- um utilizador só pode gerir a própria inscrição;
- inscrições usam o UID autenticado;
- `createdAt` deve ser um timestamp do servidor;
- inscrições só podem ser criadas para turnos abertos.

As regras devem ser publicadas antes de usar dados reais. Não uses regras
Firestore permissivas em produção.

## Estado do projeto

Este é um projeto em desenvolvimento. A funcionalidade principal de
autenticação, turnos, inscrições e disponibilidade está implementada, mas ainda
existem melhorias planeadas:

- aumentar a cobertura de testes;
- adicionar testes das Firestore Rules com o Emulator Suite;
- separar melhor a lógica de UI e acesso a dados;
- melhorar a apresentação dos turnos e a acessibilidade;
- completar o fluxo de registo com todos os dados do perfil.

## Licença

Ainda não foi definida uma licença para este projeto.
