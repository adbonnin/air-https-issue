package
{
	import mx.collections.IList;
	import mx.utils.StringUtil;
	import utils.StringUtil2;

	[Bindable]
	public class TestValue
	{
		public static const DEFAULT_RESULT:String = "/";
		
		public var value:String;
		
		public var result:String = DEFAULT_RESULT;
		
		public var error:String;
		
		function TestValue(value:String) 
		{
			this.value = value;	
		}
		
		public function resetResult():void 
		{
			this.result = DEFAULT_RESULT;
		}
		
		public function printReport(maxLength:int, separator:String, recommanded:Boolean):String
		{
			return "" + 
				StringUtil2.padding(value, maxLength, separator) + " |" + 
				StringUtil2.padding(result, 13, separator) + " |" +
				(recommanded ? "RECOMMENDED" : "");	
		}
		
		public static function resetResults(testValues:IList):void 
		{
			if (testValues == null) 
			{
				return;
			}
			
			for each (var testValue:TestValue in testValues)
			{ 
				testValue.resetResult();
			}
		}
		
		public static function maxValueLength(testValues:IList): int
		{
			if (testValues == null) 
			{
				return 0;
			}
			
			var maxLength:int = 0;
			
			for each (var testValue:TestValue in testValues)
			{
				var length: int = testValue != null && testValue.value != null 
					? testValue.value.length
					: 0;
				
				if (length > maxLength)
				{
					maxLength = length;
				}
			}
			
			return maxLength;
		}
	}
}