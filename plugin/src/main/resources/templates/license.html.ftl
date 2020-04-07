<#-- @ftlvariable name="title" type="String" -->
<#-- @ftlvariable name="artifacts" type="java.util.List<io.github.jmatsu.license.poko.DisplayArtifact>" -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <style>
        * {
            box-sizing: border-box;
            background-color: #ffffff;
            padding: 0;
            margin: 0;
        }
        .container {
            padding: 4px 4px;
        }
        .card {
            background-color: #fefefe;
            box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.1);
            padding: 4px;
            margin: 4px 0;
        }
        p {
            margin: 4px;
            padding: 0;
            word-break: break-all;
        }
        p.name {
            font-size: 18px;
        }
        p.copyright {
            font-size: 14px;
        }
        p.artifact {
            font-size: 14px;
        }
        ul {
            list-style-type: none;
        }
        ul.licenses {
            margin: 6px;
        }
        li.license {
            font-size: 14px;
        }
    </style>
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
</head>
<body>

<div class="container">
<#list artifacts as artifact>
    <div class="card">
        <p class="name">${artifact.displayName}</p>
        <#if artifact.copyrightHolders?has_content><p class="copyright">Copyright :  ${artifact.copyrightHolders?join(", ")}</p></#if>
        <p class="artifact"><#if artifact.url??><a href="${artifact.url}">${artifact.key}</a><#else>${artifact.key}</#if></p>
        <#if artifact.licenses?size != 0 >
        <ul class="licenses">
            <#list artifact.licenses as license>
                <li class="license">
                    - Under <a href="${license.url}">${license.name}</a>
                </li>
            </#list>
        </ul>
        </#if>
    </div>
</#list>
</div>
</body>
</html>