# Anonymous news contract fixtures

Observed against the public, unauthenticated Dongqiudi endpoints on 2026-09-01.
These fixtures preserve only the response shape and pagination rules. All IDs,
names, article text, comments, cursors, and media paths are synthetic.

Covered endpoints:

- `GET /v3/archive/app/tabs/getlists?id={tabId}&platform=android`
- `GET /v2/article/detail/{articleId}`
- `GET /v2/article/{articleId}/comment`

The app sends no account token, device identifier, cookie, or write request for
these flows.
