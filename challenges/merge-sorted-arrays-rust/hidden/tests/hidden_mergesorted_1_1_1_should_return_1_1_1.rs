use challenge::merge_sorted;

#[test]
fn hidden_mergesorted_1_1_1_should_return_1_1_1() {
    assert_eq!(merge_sorted(&[1, 1], &[1]), vec![1, 1, 1]);
}
