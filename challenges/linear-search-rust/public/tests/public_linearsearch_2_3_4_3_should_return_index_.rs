use challenge::linear_search;

#[test]
fn public_linearsearch_2_3_4_3_should_return_index_() {
    assert_eq!(linear_search(&[2, 3, 4], 3), 1);
}
