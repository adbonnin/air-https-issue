package utils
{
	public class StringUtil2
	{		
		public static function padding(str:String, padding:int, separator:String = " "): String
		{
			var result:String = str;
			
			while (result.length < padding) 
			{
				result += separator
			}
			
			return result;
		}
	}
}