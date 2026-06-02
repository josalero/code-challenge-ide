use challenge::linear_search;

#[test]
fn public_linearsearch_1_2_5_should_return_index_1() {
    assert_eq!(linear_search(&[1, 2], 5), -1);
}
