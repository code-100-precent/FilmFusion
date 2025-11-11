package ${packageName};

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

<#assign importTypes = []>
<#list fields as f>
    <#assign t = f.fullType?replace("[]", "")>
    <#if t?starts_with("java.") && !t?starts_with("java.lang.")>
        <#if !(importTypes?seq_contains(t))>
            <#assign importTypes += [t]>
        </#if>
    </#if>
</#list>
<#list importTypes as importType>
import ${importType};
</#list>

/**
 * ${className} 实体类
 * @author Hibiscus-code-generate
 */
@TableName("${tableName}")
public class ${className} implements Serializable {

    private static final long serialVersionUID = 1L;

<#list fields as f>
    /**
    * ${f.comment}
    */
    <#if f.primaryKey>
    @TableId
    <#else>
    @TableField("${f.originalName}")
    </#if>
    private ${f.type} ${f.name};

</#list>

<#list fields as f>
    public ${f.type} get${f.name?cap_first}() {
    return ${f.name};
    }

    public void set${f.name?cap_first}(${f.type} ${f.name}) {
    this.${f.name} = ${f.name};
    }
</#list>
}
