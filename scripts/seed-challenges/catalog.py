"""
Curated challenge catalog inspired by (see docs/challenge-catalog-sources.md):
- https://github.com/TheAlgorithms/Java
- https://github.com/TheAlgorithms/Python
- https://github.com/TheAlgorithms/Go
- https://github.com/TheAlgorithms/C-Sharp
- https://github.com/TheAlgorithms/Rust
- https://github.com/TheAlgorithms/C-Plus-Plus
- https://github.com/type-challenges/type-challenges
- https://github.com/florinpop17/app-ideas
- https://github.com/gothinkster/realworld

Java and Python are defined here. Go, Node, TypeScript, C#, Rust, C++ are in catalog_multi*.py.
Frontend tracks: catalog_frontend*.py. Run: python3 scripts/seed-challenges/generate.py
"""

JAVA_CHALLENGES = [
    {
        "slug": "factorial",
        "title": "Factorial",
        "difficulty": "easy",
        "description": "Implement `Solution.factorial(int n)` returning `n!` for `n >= 0`. For `n == 0` return `1`.",
        "starter": """package com.challenge;

public class Solution {
    public static long factorial(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("FactorialTest", "assertEquals(1L, Solution.factorial(0));"),
            ("FactorialTest", "assertEquals(120L, Solution.factorial(5));"),
            ("FactorialTest", "assertEquals(1L, Solution.factorial(1));"),
        ],
        "hidden_tests": [
            ("FactorialHiddenTest", "assertEquals(3628800L, Solution.factorial(10));"),
            ("FactorialHiddenTest", "assertEquals(6L, Solution.factorial(3));"),
        ],
    },
    {
        "slug": "fibonacci-number",
        "title": "Fibonacci Number",
        "difficulty": "easy",
        "description": "Implement `Solution.fib(int n)` returning the n-th Fibonacci number with `fib(0)=0`, `fib(1)=1`.",
        "starter": """package com.challenge;

public class Solution {
    public static long fib(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("FibonacciTest", "assertEquals(0L, Solution.fib(0));"),
            ("FibonacciTest", "assertEquals(1L, Solution.fib(1));"),
            ("FibonacciTest", "assertEquals(5L, Solution.fib(5));"),
        ],
        "hidden_tests": [
            ("FibonacciHiddenTest", "assertEquals(55L, Solution.fib(10));"),
            ("FibonacciHiddenTest", "assertEquals(8L, Solution.fib(6));"),
        ],
    },
    {
        "slug": "is-prime",
        "title": "Prime Check",
        "difficulty": "easy",
        "description": "Implement `Solution.isPrime(int n)` — return `true` if `n` is prime (`n >= 2`).",
        "starter": """package com.challenge;

public class Solution {
    public static boolean isPrime(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("PrimeTest", "assertEquals(false, Solution.isPrime(1));"),
            ("PrimeTest", "assertEquals(true, Solution.isPrime(2));"),
            ("PrimeTest", "assertEquals(true, Solution.isPrime(17));"),
        ],
        "hidden_tests": [
            ("PrimeHiddenTest", "assertEquals(false, Solution.isPrime(15));"),
            ("PrimeHiddenTest", "assertEquals(true, Solution.isPrime(97));"),
        ],
    },
    {
        "slug": "gcd",
        "title": "Greatest Common Divisor",
        "difficulty": "easy",
        "description": "Implement `Solution.gcd(int a, int b)` returning the greatest common divisor (Euclidean algorithm).",
        "starter": """package com.challenge;

public class Solution {
    public static int gcd(int a, int b) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("GcdTest", "assertEquals(6, Solution.gcd(54, 24));"),
            ("GcdTest", "assertEquals(1, Solution.gcd(17, 13));"),
            ("GcdTest", "assertEquals(5, Solution.gcd(25, 15));"),
        ],
        "hidden_tests": [
            ("GcdHiddenTest", "assertEquals(12, Solution.gcd(48, 18));"),
            ("GcdHiddenTest", "assertEquals(7, Solution.gcd(0, 7));"),
        ],
    },
    {
        "slug": "contains-duplicate",
        "title": "Contains Duplicate",
        "difficulty": "easy",
        "description": "Implement `Solution.containsDuplicate(int[] nums)` — `true` if any value appears at least twice.",
        "starter": """package com.challenge;

public class Solution {
    public static boolean containsDuplicate(int[] nums) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("ContainsDuplicateTest", "assertEquals(true, Solution.containsDuplicate(new int[] {1,2,3,1}));"),
            ("ContainsDuplicateTest", "assertEquals(false, Solution.containsDuplicate(new int[] {1,2,3,4}));"),
        ],
        "hidden_tests": [
            ("ContainsDuplicateHiddenTest", "assertEquals(true, Solution.containsDuplicate(new int[] {1,1}));"),
            ("ContainsDuplicateHiddenTest", "assertEquals(false, Solution.containsDuplicate(new int[] {}));"),
        ],
    },
    {
        "slug": "missing-number",
        "title": "Missing Number",
        "difficulty": "easy",
        "description": "Array `nums` contains `n` distinct values in `[0, n]`. One value is missing. Return it.",
        "starter": """package com.challenge;

public class Solution {
    public static int missingNumber(int[] nums) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("MissingNumberTest", "assertEquals(2, Solution.missingNumber(new int[] {3,0,1}));"),
            ("MissingNumberTest", "assertEquals(1, Solution.missingNumber(new int[] {0}));"),
        ],
        "hidden_tests": [
            ("MissingNumberHiddenTest", "assertEquals(8, Solution.missingNumber(new int[] {9,6,4,2,3,5,7,0,1}));"),
            ("MissingNumberHiddenTest", "assertEquals(0, Solution.missingNumber(new int[] {1}));"),
        ],
    },
    {
        "slug": "single-number",
        "title": "Single Number",
        "difficulty": "easy",
        "description": "Every element appears twice except one. Implement `Solution.singleNumber(int[] nums)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int singleNumber(int[] nums) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("SingleNumberTest", "assertEquals(1, Solution.singleNumber(new int[] {2,2,1}));"),
            ("SingleNumberTest", "assertEquals(4, Solution.singleNumber(new int[] {4,1,2,1,2}));"),
        ],
        "hidden_tests": [
            ("SingleNumberHiddenTest", "assertEquals(3, Solution.singleNumber(new int[] {3}));"),
            ("SingleNumberHiddenTest", "assertEquals(5, Solution.singleNumber(new int[] {7,5,7}));"),
        ],
    },
    {
        "slug": "max-subarray",
        "title": "Maximum Subarray",
        "difficulty": "medium",
        "description": "Implement `Solution.maxSubArray(int[] nums)` returning the largest sum of any contiguous subarray (Kadane).",
        "starter": """package com.challenge;

public class Solution {
    public static int maxSubArray(int[] nums) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("MaxSubarrayTest", "assertEquals(6, Solution.maxSubArray(new int[] {-2,1,-3,4,-1,2,1,-5,4}));"),
            ("MaxSubarrayTest", "assertEquals(1, Solution.maxSubArray(new int[] {1}));"),
        ],
        "hidden_tests": [
            ("MaxSubarrayHiddenTest", "assertEquals(23, Solution.maxSubArray(new int[] {5,4,-1,7,8}));"),
            ("MaxSubarrayHiddenTest", "assertEquals(-2, Solution.maxSubArray(new int[] {-2,-1}));"),
        ],
    },
    {
        "slug": "climbing-stairs",
        "title": "Climbing Stairs",
        "difficulty": "easy",
        "description": "You can climb 1 or 2 steps. Implement `Solution.climbStairs(int n)` — number of distinct ways to reach step `n`.",
        "starter": """package com.challenge;

public class Solution {
    public static int climbStairs(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("ClimbingStairsTest", "assertEquals(2, Solution.climbStairs(2));"),
            ("ClimbingStairsTest", "assertEquals(3, Solution.climbStairs(3));"),
        ],
        "hidden_tests": [
            ("ClimbingStairsHiddenTest", "assertEquals(89, Solution.climbStairs(10));"),
            ("ClimbingStairsHiddenTest", "assertEquals(1, Solution.climbStairs(1));"),
        ],
    },
    {
        "slug": "best-time-buy-sell-stock",
        "title": "Best Time to Buy and Sell Stock",
        "difficulty": "easy",
        "description": "Given daily prices, return maximum profit from one buy and one sell. `Solution.maxProfit(int[] prices)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int maxProfit(int[] prices) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("MaxProfitTest", "assertEquals(5, Solution.maxProfit(new int[] {7,1,5,3,6,4}));"),
            ("MaxProfitTest", "assertEquals(0, Solution.maxProfit(new int[] {7,6,4,3,1}));"),
        ],
        "hidden_tests": [
            ("MaxProfitHiddenTest", "assertEquals(4, Solution.maxProfit(new int[] {1,2,3,4,5}));"),
            ("MaxProfitHiddenTest", "assertEquals(0, Solution.maxProfit(new int[] {2,2,2}));"),
        ],
    },
    {
        "slug": "valid-anagram",
        "title": "Valid Anagram",
        "difficulty": "easy",
        "description": "Implement `Solution.isAnagram(String s, String t)` — `true` if `t` is an anagram of `s`.",
        "starter": """package com.challenge;

public class Solution {
    public static boolean isAnagram(String s, String t) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("AnagramTest", "assertEquals(true, Solution.isAnagram(\"anagram\", \"nagaram\"));"),
            ("AnagramTest", "assertEquals(false, Solution.isAnagram(\"rat\", \"car\"));"),
        ],
        "hidden_tests": [
            ("AnagramHiddenTest", "assertEquals(true, Solution.isAnagram(\"listen\", \"silent\"));"),
            ("AnagramHiddenTest", "assertEquals(true, Solution.isAnagram(\"\", \"\"));"),
        ],
    },
    {
        "slug": "longest-common-prefix",
        "title": "Longest Common Prefix",
        "difficulty": "easy",
        "description": "Implement `Solution.longestCommonPrefix(String[] strs)` for an array of strings.",
        "starter": """package com.challenge;

public class Solution {
    public static String longestCommonPrefix(String[] strs) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("LcpTest", "assertEquals(\"fl\", Solution.longestCommonPrefix(new String[] {\"flower\",\"flow\",\"flight\"}));"),
            ("LcpTest", "assertEquals(\"\", Solution.longestCommonPrefix(new String[] {\"dog\",\"racecar\",\"car\"}));"),
        ],
        "hidden_tests": [
            ("LcpHiddenTest", "assertEquals(\"\", Solution.longestCommonPrefix(new String[] {}));"),
            ("LcpHiddenTest", "assertEquals(\"a\", Solution.longestCommonPrefix(new String[] {\"a\"}));"),
        ],
    },
    {
        "slug": "roman-to-integer",
        "title": "Roman to Integer",
        "difficulty": "medium",
        "description": "Convert a Roman numeral string to an integer. Implement `Solution.romanToInt(String s)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int romanToInt(String s) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("RomanTest", "assertEquals(3, Solution.romanToInt(\"III\"));"),
            ("RomanTest", "assertEquals(58, Solution.romanToInt(\"LVIII\"));"),
        ],
        "hidden_tests": [
            ("RomanHiddenTest", "assertEquals(1994, Solution.romanToInt(\"MCMXCIV\"));"),
            ("RomanHiddenTest", "assertEquals(9, Solution.romanToInt(\"IX\"));"),
        ],
    },
    {
        "slug": "sqrt-integer",
        "title": "Sqrt(x)",
        "difficulty": "easy",
        "description": "Implement `Solution.mySqrt(int x)` returning the integer square root (floor).",
        "starter": """package com.challenge;

public class Solution {
    public static int mySqrt(int x) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("SqrtTest", "assertEquals(2, Solution.mySqrt(8));"),
            ("SqrtTest", "assertEquals(0, Solution.mySqrt(0));"),
        ],
        "hidden_tests": [
            ("SqrtHiddenTest", "assertEquals(3, Solution.mySqrt(10));"),
            ("SqrtHiddenTest", "assertEquals(46340, Solution.mySqrt(2147483647));"),
        ],
    },
    {
        "slug": "power-of-two",
        "title": "Power of Two",
        "difficulty": "easy",
        "description": "Implement `Solution.isPowerOfTwo(int n)` — `true` if `n` is a power of two (`n > 0`).",
        "starter": """package com.challenge;

public class Solution {
    public static boolean isPowerOfTwo(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("PowerOfTwoTest", "assertEquals(true, Solution.isPowerOfTwo(1));"),
            ("PowerOfTwoTest", "assertEquals(false, Solution.isPowerOfTwo(3));"),
        ],
        "hidden_tests": [
            ("PowerOfTwoHiddenTest", "assertEquals(true, Solution.isPowerOfTwo(16));"),
            ("PowerOfTwoHiddenTest", "assertEquals(false, Solution.isPowerOfTwo(0));"),
        ],
    },
    {
        "slug": "majority-element",
        "title": "Majority Element",
        "difficulty": "easy",
        "description": "Element appearing more than `n/2` times exists. Return it via `Solution.majorityElement(int[] nums)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int majorityElement(int[] nums) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("MajorityTest", "assertEquals(3, Solution.majorityElement(new int[] {3,2,3}));"),
            ("MajorityTest", "assertEquals(2, Solution.majorityElement(new int[] {2,2,1,1,1,2,2}));"),
        ],
        "hidden_tests": [
            ("MajorityHiddenTest", "assertEquals(1, Solution.majorityElement(new int[] {1}));"),
            ("MajorityHiddenTest", "assertEquals(5, Solution.majorityElement(new int[] {5,5,5,2,2}));"),
        ],
    },
    {
        "slug": "move-zeroes",
        "title": "Move Zeroes",
        "difficulty": "easy",
        "description": "Move all `0` to the end while keeping relative order of non-zero elements. Modify `nums` in place; return void.",
        "starter": """package com.challenge;

public class Solution {
    public static void moveZeroes(int[] nums) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("MoveZeroesTest", """
        int[] a = {0,1,0,3,12};
        Solution.moveZeroes(a);
        assertArrayEquals(new int[] {1,3,12,0}, a);
        """),
        ],
        "hidden_tests": [
            ("MoveZeroesHiddenTest", """
        int[] a = {0,0,1};
        Solution.moveZeroes(a);
        assertArrayEquals(new int[] {1,0,0}, a);
        """),
        ],
    },
    {
        "slug": "merge-sorted-arrays",
        "title": "Merge Sorted Arrays",
        "difficulty": "easy",
        "description": "Implement `Solution.merge(int[] nums1, int m, int[] nums2, int n)` merging `nums2` into `nums1` (length `m+n`, trailing zeros).",
        "starter": """package com.challenge;

public class Solution {
    public static void merge(int[] nums1, int m, int[] nums2, int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("MergeTest", """
        int[] a = {1,2,3,0,0,0};
        Solution.merge(a, 3, new int[] {2,5,6}, 3);
        assertArrayEquals(new int[] {1,2,2,3,5,6}, a);
        """),
        ],
        "hidden_tests": [
            ("MergeHiddenTest", """
        int[] a = {1,0,0,0};
        Solution.merge(a, 1, new int[] {2,3}, 2);
        assertArrayEquals(new int[] {1,2,3}, a);
        """),
        ],
    },
    {
        "slug": "hamming-weight",
        "title": "Hamming Weight",
        "difficulty": "easy",
        "description": "Count the number of `1` bits in the binary representation of `n`. `Solution.hammingWeight(int n)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int hammingWeight(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("HammingTest", "assertEquals(3, Solution.hammingWeight(11));"),
            ("HammingTest", "assertEquals(1, Solution.hammingWeight(128));"),
        ],
        "hidden_tests": [
            ("HammingHiddenTest", "assertEquals(31, Solution.hammingWeight(-1));"),
            ("HammingHiddenTest", "assertEquals(0, Solution.hammingWeight(0));"),
        ],
    },
    {
        "slug": "reverse-integer",
        "title": "Reverse Integer",
        "difficulty": "medium",
        "description": "Reverse digits of signed 32-bit integer `x`. Return `0` on overflow. `Solution.reverse(int x)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int reverse(int x) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("ReverseIntTest", "assertEquals(321, Solution.reverse(123));"),
            ("ReverseIntTest", "assertEquals(-321, Solution.reverse(-123));"),
        ],
        "hidden_tests": [
            ("ReverseIntHiddenTest", "assertEquals(0, Solution.reverse(1534236469));"),
            ("ReverseIntHiddenTest", "assertEquals(21, Solution.reverse(120));"),
        ],
    },
    {
        "slug": "linear-search",
        "title": "Linear Search",
        "difficulty": "easy",
        "description": "Return index of `target` in `nums`, or `-1` if absent. `Solution.linearSearch(int[] nums, int target)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int linearSearch(int[] nums, int target) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("LinearSearchTest", "assertEquals(1, Solution.linearSearch(new int[] {2,3,4}, 3));"),
            ("LinearSearchTest", "assertEquals(-1, Solution.linearSearch(new int[] {1,2}, 5));"),
        ],
        "hidden_tests": [
            ("LinearSearchHiddenTest", "assertEquals(0, Solution.linearSearch(new int[] {9}, 9));"),
            ("LinearSearchHiddenTest", "assertEquals(-1, Solution.linearSearch(new int[] {}, 1));"),
        ],
    },
    {
        "slug": "plus-one",
        "title": "Plus One",
        "difficulty": "easy",
        "description": "Increment a large integer represented as digit array (MSD first). Modify and return `digits`. `Solution.plusOne(int[] digits)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int[] plusOne(int[] digits) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("PlusOneTest", "assertArrayEquals(new int[] {1,2,4}, Solution.plusOne(new int[] {1,2,3}));"),
            ("PlusOneTest", "assertArrayEquals(new int[] {1}, Solution.plusOne(new int[] {0}));"),
        ],
        "hidden_tests": [
            ("PlusOneHiddenTest", "assertArrayEquals(new int[] {1,0,0,0}, Solution.plusOne(new int[] {9,9,9}));"),
            ("PlusOneHiddenTest", "assertArrayEquals(new int[] {10}, Solution.plusOne(new int[] {9}));"),
        ],
    },
    {
        "slug": "remove-duplicates-sorted",
        "title": "Remove Duplicates from Sorted Array",
        "difficulty": "easy",
        "description": "Remove duplicates in-place from sorted `nums`. Return count of unique elements. `Solution.removeDuplicates(int[] nums)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int removeDuplicates(int[] nums) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("RemoveDupTest", """
        int[] a = {1,1,2};
        assertEquals(2, Solution.removeDuplicates(a));
        assertEquals(1, a[0]);
        assertEquals(2, a[1]);
        """),
        ],
        "hidden_tests": [
            ("RemoveDupHiddenTest", """
        int[] a = {0,0,1,1,1,2,2,3,3,4};
        assertEquals(5, Solution.removeDuplicates(a));
        """),
        ],
    },
    {
        "slug": "search-insert-position",
        "title": "Search Insert Position",
        "difficulty": "easy",
        "description": "Sorted array without duplicates. Return index if `target` found else insert position. `Solution.searchInsert(int[] nums, int target)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int searchInsert(int[] nums, int target) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("SearchInsertTest", "assertEquals(2, Solution.searchInsert(new int[] {1,3,5,6}, 5));"),
            ("SearchInsertTest", "assertEquals(1, Solution.searchInsert(new int[] {1,3,5,6}, 2));"),
        ],
        "hidden_tests": [
            ("SearchInsertHiddenTest", "assertEquals(4, Solution.searchInsert(new int[] {1,3,5,6}, 7));"),
            ("SearchInsertHiddenTest", "assertEquals(0, Solution.searchInsert(new int[] {1,3,5,6}, 0));"),
        ],
    },
    {
        "slug": "happy-number",
        "title": "Happy Number",
        "difficulty": "easy",
        "description": "Replace `n` with sum of squares of its digits until `1` (happy) or loop (not). `Solution.isHappy(int n)`.",
        "starter": """package com.challenge;

public class Solution {
    public static boolean isHappy(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("HappyTest", "assertEquals(true, Solution.isHappy(19));"),
            ("HappyTest", "assertEquals(false, Solution.isHappy(2));"),
        ],
        "hidden_tests": [
            ("HappyHiddenTest", "assertEquals(true, Solution.isHappy(1));"),
            ("HappyHiddenTest", "assertEquals(false, Solution.isHappy(4));"),
        ],
    },
    {
        "slug": "count-primes",
        "title": "Count Primes",
        "difficulty": "medium",
        "description": "Count primes strictly less than `n`. `Solution.countPrimes(int n)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int countPrimes(int n) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("CountPrimesTest", "assertEquals(4, Solution.countPrimes(10));"),
            ("CountPrimesTest", "assertEquals(0, Solution.countPrimes(0));"),
        ],
        "hidden_tests": [
            ("CountPrimesHiddenTest", "assertEquals(25, Solution.countPrimes(100));"),
            ("CountPrimesHiddenTest", "assertEquals(0, Solution.countPrimes(2));"),
        ],
    },
    {
        "slug": "rotate-array",
        "title": "Rotate Array",
        "difficulty": "medium",
        "description": "Rotate `nums` right by `k` steps in-place. `Solution.rotate(int[] nums, int k)`.",
        "starter": """package com.challenge;

public class Solution {
    public static void rotate(int[] nums, int k) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("RotateTest", """
        int[] a = {1,2,3,4,5,6,7};
        Solution.rotate(a, 3);
        assertArrayEquals(new int[] {5,6,7,1,2,3,4}, a);
        """),
        ],
        "hidden_tests": [
            ("RotateHiddenTest", """
        int[] a = {-1,-100,3,99};
        Solution.rotate(a, 2);
        assertArrayEquals(new int[] {3,99,-1,-100}, a);
        """),
        ],
    },
    {
        "slug": "product-except-self",
        "title": "Product of Array Except Self",
        "difficulty": "medium",
        "description": "Return array `answer` where `answer[i]` is product of all elements except `nums[i]` (no division). `Solution.productExceptSelf(int[] nums)`.",
        "starter": """package com.challenge;

public class Solution {
    public static int[] productExceptSelf(int[] nums) {
        throw new UnsupportedOperationException("TODO");
    }
}
""",
        "public_tests": [
            ("ProductExceptTest", "assertArrayEquals(new int[] {24,12,8,6}, Solution.productExceptSelf(new int[] {1,2,3,4}));"),
            ("ProductExceptTest", "assertArrayEquals(new int[] {0,0,9,0}, Solution.productExceptSelf(new int[] {-1,1,0,-3,3}));"),
        ],
        "hidden_tests": [
            ("ProductExceptHiddenTest", "assertArrayEquals(new int[] {1}, Solution.productExceptSelf(new int[] {5}));"),
            ("ProductExceptHiddenTest", "assertArrayEquals(new int[] {2,2}, Solution.productExceptSelf(new int[] {2,2}));"),
        ],
    },
]

PYTHON_CHALLENGES = [
    {
        "slug": "factorial-python",
        "title": "Factorial (Python)",
        "difficulty": "easy",
        "description": "Implement `factorial(n: int) -> int` returning `n!` for `n >= 0`.",
        "starter": """def factorial(n: int) -> int:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_zero", "assert factorial(0) == 1"),
            ("test_five", "assert factorial(5) == 120"),
        ],
        "hidden_tests": [
            ("test_ten", "assert factorial(10) == 3628800"),
            ("test_one", "assert factorial(1) == 1"),
        ],
    },
    {
        "slug": "fibonacci-python",
        "title": "Fibonacci (Python)",
        "difficulty": "easy",
        "description": "Return the n-th Fibonacci number (`fib(0)=0`, `fib(1)=1`).",
        "starter": """def fib(n: int) -> int:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_fib_zero", "assert fib(0) == 0"),
            ("test_fib_five", "assert fib(5) == 5"),
        ],
        "hidden_tests": [
            ("test_fib_ten", "assert fib(10) == 55"),
            ("test_fib_one", "assert fib(1) == 1"),
        ],
    },
    {
        "slug": "is-prime-python",
        "title": "Prime Check (Python)",
        "difficulty": "easy",
        "description": "Return whether `n >= 2` is prime.",
        "starter": """def is_prime(n: int) -> bool:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_not_prime_one", "assert is_prime(1) is False"),
            ("test_prime_seventeen", "assert is_prime(17) is True"),
        ],
        "hidden_tests": [
            ("test_composite", "assert is_prime(15) is False"),
            ("test_prime_ninety_seven", "assert is_prime(97) is True"),
        ],
    },
    {
        "slug": "gcd-python",
        "title": "GCD (Python)",
        "difficulty": "easy",
        "description": "Greatest common divisor of `a` and `b`.",
        "starter": """def gcd(a: int, b: int) -> int:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_gcd_sample", "assert gcd(54, 24) == 6"),
            ("test_gcd_coprime", "assert gcd(17, 13) == 1"),
        ],
        "hidden_tests": [
            ("test_gcd_zero", "assert gcd(0, 7) == 7"),
            ("test_gcd_equal", "assert gcd(12, 12) == 12"),
        ],
    },
    {
        "slug": "reverse-string-python",
        "title": "Reverse String (Python)",
        "difficulty": "easy",
        "description": "Reverse the string `s` and return it.",
        "starter": """def reverse_string(s: str) -> str:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_hello", 'assert reverse_string("hello") == "olleh"'),
            ("test_empty", 'assert reverse_string("") == ""'),
        ],
        "hidden_tests": [
            ("test_palindrome", 'assert reverse_string("aba") == "aba"'),
            ("test_java", 'assert reverse_string("Java") == "avaJ"'),
        ],
    },
    {
        "slug": "is-palindrome-python",
        "title": "Palindrome String (Python)",
        "difficulty": "easy",
        "description": "Return `True` if `s` reads the same forward and backward (ignore case optional — use exact match).",
        "starter": """def is_palindrome(s: str) -> bool:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_racecar", 'assert is_palindrome("racecar") is True'),
            ("test_hello", 'assert is_palindrome("hello") is False'),
        ],
        "hidden_tests": [
            ("test_empty", 'assert is_palindrome("") is True'),
            ("test_single", 'assert is_palindrome("a") is True'),
        ],
    },
    {
        "slug": "count-vowels",
        "title": "Count Vowels",
        "difficulty": "easy",
        "description": "Count vowels `a,e,i,o,u` (lowercase) in `text`.",
        "starter": """def count_vowels(text: str) -> int:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_hello", "assert count_vowels(\"hello\") == 2"),
            ("test_empty", "assert count_vowels(\"\") == 0"),
        ],
        "hidden_tests": [
            ("test_aeiou", "assert count_vowels(\"aeiou\") == 5"),
            ("test_no_vowels", "assert count_vowels(\"xyz\") == 0"),
        ],
    },
    {
        "slug": "find-max-list",
        "title": "Find Maximum in List",
        "difficulty": "easy",
        "description": "Return the maximum integer in non-empty list `values`.",
        "starter": """def find_max(values: list[int]) -> int:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_max_basic", "assert find_max([1, 5, 3]) == 5"),
            ("test_max_negative", "assert find_max([-3, -1, -7]) == -1"),
        ],
        "hidden_tests": [
            ("test_single", "assert find_max([42]) == 42"),
            ("test_duplicates", "assert find_max([2, 2, 2]) == 2"),
        ],
    },
    {
        "slug": "flatten-nested-list",
        "title": "Flatten Nested List",
        "difficulty": "medium",
        "description": "Flatten a list that may contain integers or nested lists of integers (one level deep).",
        "starter": """def flatten(items: list) -> list[int]:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_flat", "assert flatten([1, [2, 3], 4]) == [1, 2, 3, 4]"),
            ("test_empty", "assert flatten([]) == []"),
        ],
        "hidden_tests": [
            ("test_nested_only", "assert flatten([[1, 2], [3]]) == [1, 2, 3]"),
            ("test_no_nested", "assert flatten([5, 6]) == [5, 6]"),
        ],
    },
    {
        "slug": "caesar-cipher",
        "title": "Caesar Cipher",
        "difficulty": "medium",
        "description": "Shift lowercase letters a-z by `shift` positions (wrap within alphabet). Leave other chars unchanged.",
        "starter": """def caesar_cipher(text: str, shift: int) -> str:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_shift_three", 'assert caesar_cipher("abc", 3) == "def"'),
            ("test_wrap", 'assert caesar_cipher("xyz", 3) == "abc"'),
        ],
        "hidden_tests": [
            ("test_preserve_space", 'assert caesar_cipher("a b", 1) == "b c"'),
            ("test_zero_shift", 'assert caesar_cipher("hello", 0) == "hello"'),
        ],
    },
    {
        "slug": "temperature-converter",
        "title": "Temperature Converter",
        "difficulty": "easy",
        "description": "Convert Celsius to Fahrenheit: `F = C * 9/5 + 32`. Return float.",
        "starter": """def celsius_to_fahrenheit(c: float) -> float:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_freezing", "assert celsius_to_fahrenheit(0) == 32.0"),
            ("test_boiling", "assert abs(celsius_to_fahrenheit(100) - 212.0) < 0.001"),
        ],
        "hidden_tests": [
            ("test_negative", "assert abs(celsius_to_fahrenheit(-40) - (-40.0)) < 0.001"),
            ("test_body", "assert abs(celsius_to_fahrenheit(37) - 98.6) < 0.2"),
        ],
    },
    {
        "slug": "sum-of-digits",
        "title": "Sum of Digits",
        "difficulty": "easy",
        "description": "Return sum of decimal digits of non-negative integer `n`.",
        "starter": """def sum_of_digits(n: int) -> int:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_123", "assert sum_of_digits(123) == 6"),
            ("test_zero", "assert sum_of_digits(0) == 0"),
        ],
        "hidden_tests": [
            ("test_large", "assert sum_of_digits(9999) == 36"),
            ("test_single_digit", "assert sum_of_digits(7) == 7"),
        ],
    },
    {
        "slug": "armstrong-number",
        "title": "Armstrong Number",
        "difficulty": "medium",
        "description": "Return `True` if `n` equals sum of its digits each raised to the power of digit count.",
        "starter": """def is_armstrong(n: int) -> bool:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_153", "assert is_armstrong(153) is True"),
            ("test_10", "assert is_armstrong(10) is False"),
        ],
        "hidden_tests": [
            ("test_9474", "assert is_armstrong(9474) is True"),
            ("test_1", "assert is_armstrong(1) is True"),
        ],
    },
    {
        "slug": "contains-duplicate-python",
        "title": "Contains Duplicate (Python)",
        "difficulty": "easy",
        "description": "Return `True` if any value appears at least twice in `nums`.",
        "starter": """def contains_duplicate(nums: list[int]) -> bool:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_dup", "assert contains_duplicate([1, 2, 3, 1]) is True"),
            ("test_unique", "assert contains_duplicate([1, 2, 3]) is False"),
        ],
        "hidden_tests": [
            ("test_pair", "assert contains_duplicate([1, 1]) is True"),
            ("test_empty", "assert contains_duplicate([]) is False"),
        ],
    },
    {
        "slug": "linear-search-python",
        "title": "Linear Search (Python)",
        "difficulty": "easy",
        "description": "Return index of `target` in `nums` or `-1`.",
        "starter": """def linear_search(nums: list[int], target: int) -> int:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_found", "assert linear_search([2, 3, 4], 3) == 1"),
            ("test_missing", "assert linear_search([1, 2], 5) == -1"),
        ],
        "hidden_tests": [
            ("test_first", "assert linear_search([9], 9) == 0"),
            ("test_empty", "assert linear_search([], 1) == -1"),
        ],
    },
    {
        "slug": "binary-search-python",
        "title": "Binary Search (Python)",
        "difficulty": "easy",
        "description": "Return index of `target` in sorted `nums`, or `-1`.",
        "starter": """def binary_search(nums: list[int], target: int) -> int:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_found", "assert binary_search([-1, 0, 3, 5, 9], 3) == 2"),
            ("test_missing", "assert binary_search([-1, 0, 3, 5, 9], 2) == -1"),
        ],
        "hidden_tests": [
            ("test_single", "assert binary_search([5], 5) == 0"),
            ("test_empty", "assert binary_search([], 1) == -1"),
        ],
    },
    {
        "slug": "word-frequency",
        "title": "Word Frequency",
        "difficulty": "easy",
        "description": "Given lowercase words separated by spaces, return dict mapping word -> count.",
        "starter": """def word_frequency(text: str) -> dict[str, int]:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_two_words", 'assert word_frequency("a a b") == {"a": 2, "b": 1}'),
            ("test_empty", 'assert word_frequency("") == {}'),
        ],
        "hidden_tests": [
            ("test_single", 'assert word_frequency("hello") == {"hello": 1}'),
            ("test_many", 'assert word_frequency("x y x")["x"] == 2'),
        ],
    },
    {
        "slug": "merge-sorted-lists",
        "title": "Merge Sorted Lists",
        "difficulty": "easy",
        "description": "Merge two sorted lists into one sorted list.",
        "starter": """def merge_sorted(a: list[int], b: list[int]) -> list[int]:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_merge", "assert merge_sorted([1, 3], [2, 4]) == [1, 2, 3, 4]"),
            ("test_empty_a", "assert merge_sorted([], [1]) == [1]"),
        ],
        "hidden_tests": [
            ("test_both_empty", "assert merge_sorted([], []) == []"),
            ("test_duplicates", "assert merge_sorted([1, 1], [1]) == [1, 1, 1]"),
        ],
    },
    {
        "slug": "even-odd-partition",
        "title": "Even Odd Partition",
        "difficulty": "easy",
        "description": "Return tuple `(evens, odds)` preserving order within each group.",
        "starter": """def partition_even_odd(values: list[int]) -> tuple[list[int], list[int]]:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_mix", "assert partition_even_odd([1, 2, 3, 4]) == ([2, 4], [1, 3])"),
            ("test_empty", "assert partition_even_odd([]) == ([], [])"),
        ],
        "hidden_tests": [
            ("test_all_even", "assert partition_even_odd([2, 4]) == ([2, 4], [])"),
            ("test_zero", "assert partition_even_odd([0, 1])[0] == [0]"),
        ],
    },
    {
        "slug": "two-sum-python",
        "title": "Two Sum (Python)",
        "difficulty": "easy",
        "description": "Return indices of two numbers that add to `target` (exactly one solution, use first valid pair).",
        "starter": """def two_sum(nums: list[int], target: int) -> list[int]:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_basic", "assert two_sum([2, 7, 11, 15], 9) == [0, 1]"),
            ("test_later", "assert two_sum([3, 2, 4], 6) == [1, 2]"),
        ],
        "hidden_tests": [
            ("test_dup", "assert two_sum([3, 3], 6) == [0, 1]"),
            ("test_negative", "assert two_sum([-1, -2, -3], -5) == [1, 2]"),
        ],
    },
    {
        "slug": "valid-parentheses-python",
        "title": "Valid Parentheses (Python)",
        "difficulty": "easy",
        "description": "Return whether bracket string `()` `{}` `[]` is valid and properly nested.",
        "starter": """def is_valid_parentheses(s: str) -> bool:
    raise NotImplementedError("TODO")
""",
        "public_tests": [
            ("test_simple", 'assert is_valid_parentheses("()") is True'),
            ("test_mixed", 'assert is_valid_parentheses("()[]{}") is True'),
        ],
        "hidden_tests": [
            ("test_invalid", 'assert is_valid_parentheses("(]") is False'),
            ("test_nested", 'assert is_valid_parentheses("{[]}") is True'),
        ],
    },
]
