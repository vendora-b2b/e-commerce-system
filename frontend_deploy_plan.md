- Frontend will deploy as a ECS task
- Frontend just use relative path to call backend, browser automatically map the domain to the endpoint

### Verify how to do that:
1. Verify relative path front end
2. Read the code of ECS task to do that (first plan is to user the ECS task and then migrate to lambda + S3 + cloudfront solution).
3. 