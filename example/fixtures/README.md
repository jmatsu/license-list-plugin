## inspection-**

Use custom resources to test the scheme

- inspection-artifact-definition.yml
- inspection-license-catalog.yml

## validate-**

- validate-artifact-definition.yml
    - `A dummy artifact` + [../app/license-list/artifact-definition.yml](../app/license-list/artifact-definition.yml)
- validate-license-catalog.yml
    - A dummy license attached to the dummy artifact above
- validate-missing-artifact-definition.yml
    - [../app/license-list/artifact-definition.yml](../license-list/artifact-definition.yml) - `at least one artifact`
