<#assign
    known = SPRING_SECURITY_CONTEXT??
>
<!-- ^^^^Spring Security помещает контекст фримаркера в специальный объект, который позволяет оперировать контекстом Spring Security -->
<#if known>
    <#assign
        user = SPRING_SECURITY_CONTEXT.authentication.principal
        name = user.getUsername()
        isAdmin = user.isAdmin()
        currentUserId = user.getId()
    >
<#else>
    <#assign
        name = "unknown"
        isAdmin = false
        currentUserId = -1
    >
</#if>