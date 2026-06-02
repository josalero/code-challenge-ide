use challenge::binary_search;

#[test]
fn hidden_binarysearch_2_4_6_2_should_return_index_() {
    assert_eq!(binary_search(&[2, 4, 6], 2), 0);
}
