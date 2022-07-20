/*
 *    Copyright 2009-2021 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * 属性分词器，即解析属性工具类
 * 比如 monthOfYear、 year.months[1].days[2]，最后都解析为 monthOfYear、year、months、days字段以及索引 1、2
 *
 * @author Clinton Begin
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {
  /**
   * 注意这些属性之间的关系：
   *   indexedName = name[index]
   */
  private String name;
  private final String indexedName;
  private String index;
  private final String children;

  /**
   * 各字段关系如下：
   *   fullname = indexedName + children
   *   indexedName = name[index]
   */
  public PropertyTokenizer(String fullname) {
    int delim = fullname.indexOf('.');
    if (delim > -1) {
      /*
       * 假设 fullname为 months[1].days[2]，到这一步:
       *   name = months[1]
       *   children = "days[2]"
       */
      name = fullname.substring(0, delim);
      children = fullname.substring(delim + 1);
    } else {
      /*
       * 假设 fullname为 monthOfYear，那么解析之后 PropertyTokenizer各属性值为：
       *   name = "monthOfYear"
       *   children = null
       *   indexedName = "monthOfYear"
       *   index = null
       */
      name = fullname;
      children = null;
    }
    indexedName = name;
    delim = name.indexOf('[');
    if (delim > -1) {
      /*
       * 假设 fullname为 months[1].days[2]，到这一步:
       *   children = "days[2]"
       *   indexedName = months[1]
       *   index = 1
       *   name = months
       */
      index = name.substring(delim + 1, name.length() - 1);
      name = name.substring(0, delim);
    }
  }

  public String getName() {
    return name;
  }

  public String getIndex() {
    return index;
  }

  public String getIndexedName() {
    return indexedName;
  }

  public String getChildren() {
    return children;
  }

  /**
   * 判断是否要继续解析，
   * 比如上面的例子，months[1].days[2] 经过一次解析后，解析出了 months、1, children = "days[2]" 还未解析，hasNext()返回true
   */
  @Override
  public boolean hasNext() {
    return children != null;
  }

  /**
   * 继续解析子属性：
   * 比如上面的例子，months[1].days[2] 经过一次解析后，解析出了 months、1,
   * children = "days[2]"，hasNext()方法返回true，需调用next()方法继续解析，解析结果如下：
   *   name = days
   *   indexedName = days[2]
   *   index = 2
   *   children = null
   */
  @Override
  public PropertyTokenizer next() {
    return new PropertyTokenizer(children);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Remove is not supported, as it has no meaning in the context of properties.");
  }
}
