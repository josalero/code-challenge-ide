use challenge::merge_sorted;

#[test]
fn public_mergesorted_1_2_3_2_5_6_should_return_1_2() {
    assert_eq!(merge_sorted(&[1, 2, 3], &[2, 5, 6]), vec![1, 2, 2, 3, 5, 6]);
}
