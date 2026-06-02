use challenge::merge_sorted;

#[test]
fn hidden_mergesorted_1_should_return_1() {
    assert_eq!(merge_sorted(&[1], &[]), vec![1]);
}
