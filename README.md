# RefactorHub

![logo](/client/src/static/logo.png)

RefactorHub is a commit annotator for refactoring.

## Demonstration

[![demo](https://img.youtube.com/vi/Ew1wVBZkpro/sddefault.jpg)](https://www.youtube.com/watch?v=Ew1wVBZkpro)

## How to Run

1. Create OAuth App [here](https://github.com/settings/developers) with `http://localhost:8080` as callback URL, and get `CLIENT_ID` and `CLIENT_SECRET`.
2. Generate access token [here](https://github.com/settings/tokens), and get `GITHUB_ACCESS_TOKEN`.
3. Set `CLIENT_ID`, `CLIENT_SECRET`, and `GITHUB_ACCESS_TOKEN` on `.env` file.
4. `docker-compose up`
5. Open http://localhost:8080

## Deploy

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy?template=https://github.com/salab/RefactorHub)
