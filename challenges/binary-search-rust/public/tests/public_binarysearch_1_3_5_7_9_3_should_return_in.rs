use challenge::binary_search;

#[test]
fn public_binarysearch_1_3_5_7_9_3_should_return_in() {
    assert_eq!(binary_search(&[1, 3, 5, 7, 9], 3), 1);
}
