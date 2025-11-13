<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="${packageName}.mapper.${className}Mapper">

    <resultMap id="BaseResultMap" type="${packageName}.model.entity.${className}">
        <#list fields as f>
            <#if f.primaryKey>
        <id column="${f.originalName}" property="${f.name}" />
            <#else>
        <result column="${f.originalName}" property="${f.name}" />
            </#if>
        </#list>
    </resultMap>

    <sql id="Base_Column_List">
        <#list fields as f>
            `${f.originalName}`<#if f_has_next>,</#if>
        </#list>
    </sql>

</mapper>
