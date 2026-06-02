use challenge::linear_search;

#[test]
fn public_cases() {
    assert_eq!(linear_search(&[2, 3, 4], 3), 1);
    assert_eq!(linear_search(&[1, 2], 5), -1);
}
