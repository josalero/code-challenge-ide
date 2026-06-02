use challenge::binary_search;

#[test]
fn hidden_binarysearch_1_should_return_index_1() {
    assert_eq!(binary_search(&[], 1), -1);
}
