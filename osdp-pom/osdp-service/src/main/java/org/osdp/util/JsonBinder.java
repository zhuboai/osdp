package org.osdp.util;

public final class JsonBinder {

	   private static class NonEmptySingletonHolder {
	      public static final JsonBinder nonEmptyBinder = new JsonBinder(Include.NON_EMPTY );
	   }

	   /**
	    * 获取只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
	    */
	   public static JsonBinder getNonEmptyBinder() {
	      return NonEmptySingletonHolder. nonEmptyBinder;
	   }

	   private ObjectMapper mapper;

	   private JsonBinder(Include include) {
	      mapper = new ObjectMapper();
	      //设置输出时包含属性的风格
	      mapper.setSerializationInclusion(include);
	      //序列化时，忽略空的bean(即沒有任何Field)
	      mapper.disable(SerializationFeature. FAIL_ON_EMPTY_BEANS);
	      //序列化时，忽略在JSON字符串中存在但Java对象实际没有的属性
	      mapper.disable(DeserializationFeature. FAIL_ON_UNKNOWN_PROPERTIES);
	      //make all member fields serializable without further annotations, instead of just public fields (default setting).
	      mapper.setVisibility(PropertyAccessor. FIELD, Visibility.ANY);
	   }

	   /**
	    * Object可以是POJO，也可以是Collection或数组。 如果对象为Null, 返回"null". 如果集合为空集合, 返回"[]".
	    */
	   public String toJson(Object object) {
	      try {
	         return mapper.writeValueAsString(object);
	      } catch (IOException e) {
	         throw new JsonSerializedException( "Serialized Object to json string error : " + object, e);
	      }
	   }

	   /**
	    * 反序列化POJO或简单Collection如List <String>. 如果JSON字符串为Null或"null"字符串, 返回Null.
	    * 如果JSON字符串为"[]", 返回空集合. 如需反序列化复杂Collection如List <MyBean>,
	    * 请使用fromJson(String,JavaType)
	    *
	    * @see #fromJson(String, JavaType)
	    */
	   public <T> T fromJson(String jsonString, Class<T> clazz) {
	      if (jsonString == null || "".equals(jsonString.trim())) {
	         return null;
	      }
	      try {
	         return mapper.readValue(jsonString, clazz);
	      } catch (IOException e) {
	         throw new JsonDeserializedException( "Deserialized json string error : " + jsonString, e);
	      }
	   }
	}
